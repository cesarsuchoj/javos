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

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for API versioning lifecycle management.
 *
 * <p>Supports the path-based versioning strategy ({@code /api/v1/...}) and enables
 * soft-deprecation of old API versions via standard HTTP response headers:
 * {@code X-API-Version}, {@code Deprecation}, {@code Sunset}, and {@code Warning}.
 *
 * <p>Example YAML to declare {@code v1} as deprecated with a sunset date:
 * <pre>{@code
 * javos:
 *   api:
 *     current-version: v2
 *     deprecated-versions:
 *       v1:
 *         sunset-date: "2026-12-31"
 *         message: "Please migrate to /api/v2/."
 * }</pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "javos.api")
public class ApiVersionProperties {

    /** The current active API version identifier (e.g. {@code "v1"}). */
    private String currentVersion = "v1";

    /**
     * Map of deprecated API versions.
     * Key is the version identifier (e.g. {@code "v1"});
     * value contains the deprecation metadata.
     */
    private Map<String, DeprecatedVersionInfo> deprecatedVersions = new HashMap<>();

    /**
     * Metadata for a deprecated API version.
     */
    @Data
    public static class DeprecatedVersionInfo {

        /**
         * ISO 8601 date after which the version will be removed (e.g. {@code "2026-12-31"}).
         * Rendered as an HTTP-date in the {@code Sunset} response header.
         */
        private String sunsetDate;

        /**
         * Optional human-readable migration hint appended to the {@code Warning} header.
         * Example: {@code "Please migrate to /api/v2/."}
         */
        private String message;
    }
}
