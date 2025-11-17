package com.example.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/api/auth/users/login",
            "/api/auth/users/register",
            "/api/auth/users/refresh",
            "/api/auth/users/logout"
    );

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/actuator",
            "/swagger",
            "/v3/api-docs",
            "/webjars"
    );

    private static final List<String> USER_ACCOUNT_PREFIXES = List.of(
            "/api/auth/users",
            "/api/auth/admin"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Thiếu token hoặc token không hợp lệ.");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
            if (jwtService.isTokenExpired(claims)) {
                return writeError(exchange, HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn.");
            }
        } catch (Exception ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token không hợp lệ.");
        }

        String userId = asString(claims.get("userId"));
        String email = claims.getSubject();
        String role = asString(claims.get("role"));
        String profileStatus = asString(claims.get("profileStatus"));

        if (requiresApprovedProfile(path, role) && !"APPROVED".equalsIgnoreCase(profileStatus)) {
            return writeError(exchange, HttpStatus.FORBIDDEN, "Hồ sơ chưa được duyệt. Vui lòng hoàn tất KYC.");
        }

        var mutatedRequest = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Email");
                    httpHeaders.remove("X-User-Role");
                    httpHeaders.remove("X-User-Profile-Status");
                    if (StringUtils.hasText(userId)) {
                        httpHeaders.add("X-User-Id", userId);
                    }
                    if (StringUtils.hasText(email)) {
                        httpHeaders.add("X-User-Email", email);
                    }
                    if (StringUtils.hasText(role)) {
                        httpHeaders.add("X-User-Role", role);
                    }
                    if (StringUtils.hasText(profileStatus)) {
                        httpHeaders.add("X-User-Profile-Status", profileStatus);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return true;
        }
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean requiresApprovedProfile(String path, String role) {
        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
            return false;
        }
        boolean isUserAccountPath = USER_ACCOUNT_PREFIXES.stream().anyMatch(path::startsWith);
        return !isUserAccountPath;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        return String.valueOf(value);
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String payload = String.format("{\"message\":\"%s\"}", message.replace("\"", "\\\""));
        var buffer = response.bufferFactory().wrap(payload.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
