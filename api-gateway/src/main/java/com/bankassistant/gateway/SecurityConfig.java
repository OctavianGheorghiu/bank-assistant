package com.bankassistant.gateway;

import java.util.Optional;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    static final String USER_HEADER = "X-User-Id";

    /** Every route needs a valid Keycloak token; only the health check is open. */
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Bearer tokens only, no cookies, so there is no session for CSRF to abuse
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /** Drops any X-User-Id the caller sent and sets it from the validated token's username. */
    @Bean
    GlobalFilter userIdHeaderFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(principal -> Optional.ofNullable(
                        ((JwtAuthenticationToken) principal).getToken().getClaimAsString("preferred_username")))
                .defaultIfEmpty(Optional.empty())
                .flatMap(userId -> chain.filter(exchange.mutate()
                        .request(request -> request.headers(headers -> {
                            headers.remove(USER_HEADER);
                            userId.ifPresent(id -> headers.set(USER_HEADER, id));
                        }))
                        .build()));
    }
}
