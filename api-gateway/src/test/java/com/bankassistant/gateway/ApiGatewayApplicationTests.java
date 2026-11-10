package com.bankassistant.gateway;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** The gateway's trust boundary: tokens are required, and the identity header comes only from the token. */
@SpringBootTest
class ApiGatewayApplicationTests {

    /** Stand-in for accounts-service: answers with the X-User-Id header it received. */
    static final HttpServer backend = startEchoBackend();

    @DynamicPropertySource
    static void routeToBackend(DynamicPropertyRegistry registry) {
        registry.add("ACCOUNTS_SERVICE_URL", () -> "http://127.0.0.1:" + backend.getAddress().getPort());
    }

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
    }

    @AfterAll
    static void stopBackend() {
        backend.stop(0);
    }

    @Test
    void requestWithoutTokenIsRejected() {
        client.get().uri("/api/accounts").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void identityHeaderComesFromTheTokenNotTheCaller() {
        client.mutateWith(mockJwt().jwt(jwt -> jwt.claim("preferred_username", "alice")))
                .get().uri("/api/accounts")
                .header("X-User-Id", "bob")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("alice");
    }

    @Test
    void healthCheckIsOpen() {
        client.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    private static HttpServer startEchoBackend() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                byte[] body = String.valueOf(exchange.getRequestHeaders().getFirst("X-User-Id"))
                        .getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
