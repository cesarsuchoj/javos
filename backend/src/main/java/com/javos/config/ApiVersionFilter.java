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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Servlet filter that implements the API versioning lifecycle strategy.
 *
 * <p>For every request to an {@code /api/{version}/...} path this filter:
 * <ol>
 *   <li>Adds {@code X-API-Version: {version}} to the response so clients always know
 *       which version processed the request.</li>
 *   <li>When the version is listed under {@code javos.api.deprecated-versions}, adds
 *       the soft-deprecation headers:
 *       <ul>
 *         <li>{@code Deprecation: true} – RFC 8594 signal that the version is deprecated.</li>
 *         <li>{@code Sunset: <HTTP-date>} – date after which the version will be removed.</li>
 *         <li>{@code Warning: 299 - "<message>"} – human-readable migration hint.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>Non-API paths (e.g. static assets, Actuator, Swagger UI) are not affected.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiVersionFilter extends OncePerRequestFilter {

    static final String API_VERSION_HEADER = "X-API-Version";
    static final String DEPRECATION_HEADER = "Deprecation";
    static final String SUNSET_HEADER = "Sunset";
    static final String WARNING_HEADER = "Warning";

    private static final String API_PATH_PREFIX = "/api/";
    private static final ZoneId GMT = ZoneId.of("GMT");

    /** RFC 7231 HTTP-date format used in the Sunset header (e.g. "Wed, 31 Dec 2026 23:59:59 GMT"). */
    private static final DateTimeFormatter HTTP_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private final ApiVersionProperties props;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith(API_PATH_PREFIX)) {
            String version = extractVersion(path);
            if (version != null) {
                response.setHeader(API_VERSION_HEADER, version);

                ApiVersionProperties.DeprecatedVersionInfo deprecatedInfo =
                        props.getDeprecatedVersions().get(version);
                if (deprecatedInfo != null) {
                    addDeprecationHeaders(request, response, version, deprecatedInfo);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the version segment from a path like {@code /api/v1/clients}.
     * Returns {@code null} when no version segment matching {@code v\d+} is present
     * (e.g. {@code /api/docs}).
     */
    String extractVersion(String path) {
        String afterPrefix = path.substring(API_PATH_PREFIX.length());
        int slashIdx = afterPrefix.indexOf('/');
        String segment = slashIdx >= 0 ? afterPrefix.substring(0, slashIdx) : afterPrefix;
        return segment.matches("v\\d+") ? segment : null;
    }

    private void addDeprecationHeaders(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String version,
                                       ApiVersionProperties.DeprecatedVersionInfo info) {
        response.setHeader(DEPRECATION_HEADER, "true");

        if (info.getSunsetDate() != null && !info.getSunsetDate().isBlank()) {
            try {
                LocalDate sunsetDate = LocalDate.parse(info.getSunsetDate());
                ZonedDateTime sunsetDateTime = sunsetDate.atTime(23, 59, 59).atZone(GMT);
                response.setHeader(SUNSET_HEADER, sunsetDateTime.format(HTTP_DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                log.warn("Invalid sunset-date '{}' configured for deprecated API version {}",
                        info.getSunsetDate(), version);
            }
        }

        String migrationPath = "/api/" + props.getCurrentVersion() + "/";
        StringBuilder warningText = new StringBuilder(
                "Deprecated API version " + version + ". Please migrate to " + migrationPath);
        if (info.getMessage() != null && !info.getMessage().isBlank()) {
            warningText.append(" ").append(info.getMessage());
        }
        response.setHeader(WARNING_HEADER, "299 - \"" + warningText + "\"");

        log.warn("Deprecated API version {} accessed: {} {}", version,
                request.getMethod(), request.getRequestURI());
    }
}
