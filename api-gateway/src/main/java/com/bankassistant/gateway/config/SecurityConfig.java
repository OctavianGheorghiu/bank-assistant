package com.bankassistant.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /** Every route needs a valid Keycloak token; only the health check is open. */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring JWT validation: every route except /actuator/health needs a token");
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Bearer tokens only, no cookies, so there is no session for CSRF to abuse
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
