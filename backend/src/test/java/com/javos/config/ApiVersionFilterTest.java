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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ApiVersionFilter}.
 */
class ApiVersionFilterTest {

    private ApiVersionProperties props;
    private ApiVersionFilter filter;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new ApiVersionProperties();
        props.setCurrentVersion("v1");
        props.setDeprecatedVersions(Map.of());
        filter = new ApiVersionFilter(props);
    }

    // -------------------------------------------------------------------------
    // extractVersion
    // -------------------------------------------------------------------------

    @Test
    void extractVersion_standardApiPath_returnsVersion() {
        assertThat(filter.extractVersion("/api/v1/clients")).isEqualTo("v1");
        assertThat(filter.extractVersion("/api/v2/users/123")).isEqualTo("v2");
        assertThat(filter.extractVersion("/api/v10/some/deep/path")).isEqualTo("v10");
    }

    @Test
    void extractVersion_pathWithoutVersion_returnsNull() {
        assertThat(filter.extractVersion("/api/docs")).isNull();
        assertThat(filter.extractVersion("/api/unknown/path")).isNull();
    }

    // -------------------------------------------------------------------------
    // X-API-Version header
    // -------------------------------------------------------------------------

    @Test
    void apiRequest_addsVersionHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader(ApiVersionFilter.API_VERSION_HEADER)).isEqualTo("v1");
    }

    @Test
    void nonApiRequest_doesNotAddVersionHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader(ApiVersionFilter.API_VERSION_HEADER)).isNull();
    }

    @Test
    void apiDocsRequest_doesNotAddVersionHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/docs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.API_VERSION_HEADER)).isNull();
    }

    // -------------------------------------------------------------------------
    // Deprecation headers
    // -------------------------------------------------------------------------

    @Test
    void currentVersion_doesNotAddDeprecationHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.DEPRECATION_HEADER)).isNull();
        assertThat(response.getHeader(ApiVersionFilter.SUNSET_HEADER)).isNull();
        assertThat(response.getHeader(ApiVersionFilter.WARNING_HEADER)).isNull();
    }

    @Test
    void deprecatedVersion_addsDeprecationAndWarningHeaders() throws Exception {
        ApiVersionProperties.DeprecatedVersionInfo info = new ApiVersionProperties.DeprecatedVersionInfo();
        info.setSunsetDate("2026-12-31");
        info.setMessage("Use /api/v2/ instead.");
        props.setDeprecatedVersions(Map.of("v1", info));
        props.setCurrentVersion("v2");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.DEPRECATION_HEADER)).isEqualTo("true");
        assertThat(response.getHeader(ApiVersionFilter.SUNSET_HEADER))
                .contains("2026").contains("GMT");
        assertThat(response.getHeader(ApiVersionFilter.WARNING_HEADER))
                .startsWith("299 - \"")
                .contains("v1")
                .contains("/api/v2/")
                .contains("Use /api/v2/ instead.");
    }

    @Test
    void deprecatedVersion_withoutSunsetDate_omitsSunsetHeader() throws Exception {
        ApiVersionProperties.DeprecatedVersionInfo info = new ApiVersionProperties.DeprecatedVersionInfo();
        info.setMessage("Upgrade soon.");
        props.setDeprecatedVersions(Map.of("v1", info));
        props.setCurrentVersion("v2");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.DEPRECATION_HEADER)).isEqualTo("true");
        assertThat(response.getHeader(ApiVersionFilter.SUNSET_HEADER)).isNull();
        assertThat(response.getHeader(ApiVersionFilter.WARNING_HEADER)).isNotBlank();
    }

    @Test
    void deprecatedVersion_withInvalidSunsetDate_omitsSunsetHeaderGracefully() throws Exception {
        ApiVersionProperties.DeprecatedVersionInfo info = new ApiVersionProperties.DeprecatedVersionInfo();
        info.setSunsetDate("not-a-date");
        props.setDeprecatedVersions(Map.of("v1", info));
        props.setCurrentVersion("v2");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.DEPRECATION_HEADER)).isEqualTo("true");
        assertThat(response.getHeader(ApiVersionFilter.SUNSET_HEADER)).isNull();
    }

    @Test
    void nonDeprecatedVersion_isNotAffectedByDeprecationConfig() throws Exception {
        ApiVersionProperties.DeprecatedVersionInfo info = new ApiVersionProperties.DeprecatedVersionInfo();
        info.setSunsetDate("2026-12-31");
        props.setDeprecatedVersions(Map.of("v1", info));
        props.setCurrentVersion("v2");

        // Request to v2 (current) must not carry deprecation headers
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/clients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(ApiVersionFilter.API_VERSION_HEADER)).isEqualTo("v2");
        assertThat(response.getHeader(ApiVersionFilter.DEPRECATION_HEADER)).isNull();
    }
}
