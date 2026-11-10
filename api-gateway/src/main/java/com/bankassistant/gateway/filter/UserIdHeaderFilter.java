package com.bankassistant.gateway.filter;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Drops any X-User-Id the caller sent and sets it from the validated token's username. */
@Slf4j
@Component
public class UserIdHeaderFilter implements GlobalFilter {

    public static final String USER_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("filter {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath());
        return exchange.getPrincipal()
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
