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

import java.util.List;

/**
 * Configurable CORS properties loaded from application.yml / environment variables.
 *
 * <p>Set {@code CORS_ALLOWED_ORIGINS} to a comma-separated list of allowed origins in
 * production (e.g. {@code https://app.example.com,https://admin.example.com}).
 * The wildcard {@code *} is the default for convenience in development only.
 */
@Data
@Component
@ConfigurationProperties(prefix = "javos.cors")
public class CorsProperties {

    /** Comma-separated list of allowed origins. Use {@code *} to allow all (dev only). */
    private List<String> allowedOrigins = List.of("*");

    /** HTTP methods permitted in cross-origin requests. */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /** Request headers permitted in cross-origin requests. Use {@code *} to allow all. */
    private List<String> allowedHeaders = List.of("*");

    /** Response headers exposed to the browser. */
    private List<String> exposedHeaders = List.of("X-Correlation-Id");

    /** Pre-flight cache duration in seconds. */
    private long maxAge = 3600L;
}
