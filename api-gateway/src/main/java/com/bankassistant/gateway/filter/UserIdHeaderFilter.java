package com.bankassistant.gateway.filter;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Drops any X-User-Id the caller sent and sets it from the validated token's username. A request whose token
 * has no username is answered 401 here, so no routed service ever sees a request without an identity.
 */
@Slf4j
@Component
public class UserIdHeaderFilter implements GlobalFilter {

    public static final String USER_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("filter {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath());
        return exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .mapNotNull(principal ->
                        ((JwtAuthenticationToken) principal).getToken().getClaimAsString("preferred_username"))
                .map(Optional::of)
                // Decided before the chain runs: chain.filter itself completes empty, so switchIfEmpty would misfire
                .defaultIfEmpty(Optional.empty())
                .flatMap(userId -> userId.isEmpty()
                        ? unauthorized(exchange)
                        // set replaces every incoming X-User-Id value, whatever its case
                        : chain.filter(exchange.mutate()
                                .request(request -> request.headers(headers -> headers.set(USER_HEADER, userId.get())))
                                .build()));
    }

    private static Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
