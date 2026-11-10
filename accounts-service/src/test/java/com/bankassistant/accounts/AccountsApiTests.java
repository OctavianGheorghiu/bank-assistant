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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Runs the real migrations and seed data against the same PostgreSQL image as Compose. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AccountsApiTests {

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
        String transactions = mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("limit", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truncated").value(false))
                .andReturn().getResponse().getContentAsString();

        List<String> amounts = JsonPath.read(transactions, "$.transactions[*].amount");
        BigDecimal sum = amounts.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(amounts).hasSizeGreaterThan(300); // about a year of activity
        assertThat(new BigDecimal(JsonPath.<String>read(balance, "$.amount"))).isEqualByComparingTo(sum);
        assertThat(sum).isPositive();
    }

    @Test
    void filtersTransactionsByCategoryIgnoringCase() throws Exception {
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("category", "Groceries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(org.hamcrest.Matchers.greaterThan(50)))
                .andExpect(jsonPath("$.transactions[*].category").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("groceries"))));
    }

    @Test
    void defaultLimitTruncatesAndSaysSo() throws Exception {
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(100))
                .andExpect(jsonPath("$.truncated").value(true));
    }

    @Test
    void impossibleFiltersAreRejected() throws Exception {
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("from", "2026-11-01").param("to", "2026-10-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/accounts/alice-current/transactions").header("X-User-Id", "alice")
                        .param("limit", "1001"))
                .andExpect(status().isBadRequest());
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
