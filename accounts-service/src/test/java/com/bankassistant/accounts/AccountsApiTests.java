package com.bankassistant.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** Runs the real migrations and seed data against the same PostgreSQL image as Compose. */
@SpringBootTest
class AccountsApiTests {

    @TestConfiguration(proxyBeanMethods = false)
    static class Database {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer(DockerImageName.parse("pgvector/pgvector:0.8.7-pg18-trixie")
                    .asCompatibleSubstituteFor("postgres"));
        }
    }

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listsOnlyTheCallersAccounts() throws Exception {
        mvc.perform(get("/accounts").header("X-User-Id", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.contains("alice-current", "alice-savings")));
    }

    @Test
    void balanceIsTheSumOfTheTransactions() throws Exception {
        String balance = mvc.perform(get("/accounts/alice-current/balance").header("X-User-Id", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andReturn().getResponse().getContentAsString();
        String transactions = mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> amounts = JsonPath.read(transactions, "$[*].amount");
        BigDecimal sum = amounts.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(amounts).hasSizeGreaterThan(300); // about a year of activity
        assertThat(new BigDecimal(JsonPath.<String>read(balance, "$.amount"))).isEqualByComparingTo(sum);
        assertThat(sum).isPositive();
    }

    @Test
    void filtersTransactionsByCategory() throws Exception {
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("category", "groceries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(50)))
                .andExpect(jsonPath("$[*].category").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("groceries"))));
    }

    @Test
    void anotherCustomersAccountIsNotFound() throws Exception {
        mvc.perform(get("/accounts/bob-current/balance").header("X-User-Id", "alice"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/accounts/bob-current/transactions").header("X-User-Id", "alice"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingIdentityHeaderIsRejected() throws Exception {
        mvc.perform(get("/accounts")).andExpect(status().isBadRequest());
    }
}
