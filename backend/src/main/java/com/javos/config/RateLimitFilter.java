/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter that enforces per-IP rate limiting using the token-bucket algorithm
 * (Bucket4j in-memory implementation).
 *
 * <p>Two independent bucket configurations are used:
 * <ul>
 *   <li><b>Auth bucket</b> – stricter, for {@code /api/v1/auth/**} paths.</li>
 *   <li><b>API bucket</b>  – general, for all other {@code /api/**} paths.</li>
 * </ul>
 *
 * <p>Static resources and non-API paths are not rate-limited.
 * Rate limiting can be disabled entirely via {@code javos.rate-limit.enabled=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";
    private static final String API_PATH_PREFIX = "/api/";

    private final RateLimitProperties props;

    /** Per-IP buckets for auth endpoints. */
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    /** Per-IP buckets for general API endpoints. */
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!props.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Only rate-limit API paths
        if (!path.startsWith(API_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        boolean isAuthPath = path.startsWith(AUTH_PATH_PREFIX);

        Bucket bucket = isAuthPath
                ? authBuckets.computeIfAbsent(clientIp, k -> createAuthBucket())
                : apiBuckets.computeIfAbsent(clientIp, k -> createApiBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
            sendRateLimitResponse(response);
        }
    }

    private Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(props.getApiCapacity())
                .refillGreedy(props.getApiRefillTokens(),
                        Duration.ofSeconds(props.getApiRefillDurationSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(props.getAuthCapacity())
                .refillGreedy(props.getAuthRefillTokens(),
                        Duration.ofSeconds(props.getAuthRefillDurationSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolves the real client IP, honouring the {@code X-Forwarded-For} header set by
     * reverse proxies (nginx, load balancers). Falls back to the remote address.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For may contain a comma-separated chain; first entry is the client
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente mais tarde.\"}");
    }
}
