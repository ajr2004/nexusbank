package com.nexusbank.api_gateway.filter;

import com.nexusbank.api_gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtils jwtUtils;

    public AuthenticationFilter(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    public static class Config {
        // Configuration fields if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 🟢 FIXED WHITELIST: Match your exact properties path mapping layout (/api/users/...)
            if (path.contains("/api/users/login") || path.contains("/api/users/register")) {
                return chain.filter(exchange);
            }

            // 🔍 1. Check if Authorization Header is present
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization Header Format");
            }

            String token = authHeader.substring(7);

            // 🔍 2. Validate Token Expiration
            if (jwtUtils.isTokenExpired(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Security session has expired");
            }

            // 🔍 3. Extract Role and protect Admin Paths
            Claims claims = jwtUtils.getClaims(token);
            String role = claims.get("role", String.class); 

            if (path.contains("/admin/") && (role == null || !role.equalsIgnoreCase("ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Administrative privileges required.");
            }

            // Forward request context cleanly downstream
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Authenticated-User", claims.getSubject())
                    .header("X-Authenticated-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }
}