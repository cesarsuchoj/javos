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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limiting configuration properties.
 *
 * <p>Two independent bucket configurations are maintained:
 * <ul>
 *   <li><b>api</b> – applied to all API endpoints (general throttling).</li>
 *   <li><b>auth</b> – stricter limits applied specifically to {@code /api/v1/auth/**}
 *       to defend against brute-force and credential-stuffing attacks.</li>
 * </ul>
 *
 * <p>Both buckets use the token-bucket algorithm and are keyed per client IP address.
 * All values are configurable via environment variables (see application.yml).
 */
@Data
@Component
@ConfigurationProperties(prefix = "javos.rate-limit")
public class RateLimitProperties {

    /** Master switch – set to {@code false} to disable rate limiting entirely. */
    private boolean enabled = true;

    // --- General API bucket ---

    /** Maximum tokens (requests) for the general API bucket. */
    private int apiCapacity = 100;

    /** Tokens refilled per period for the general API bucket. */
    private int apiRefillTokens = 100;

    /** Refill period in seconds for the general API bucket (default: 60 s → 100 req/min). */
    private int apiRefillDurationSeconds = 60;

    // --- Auth endpoint bucket (stricter) ---

    /** Maximum tokens for the auth-endpoint bucket. */
    private int authCapacity = 10;

    /** Tokens refilled per period for the auth-endpoint bucket. */
    private int authRefillTokens = 10;

    /** Refill period in seconds for the auth-endpoint bucket (default: 60 s → 10 req/min). */
    private int authRefillDurationSeconds = 60;
}
