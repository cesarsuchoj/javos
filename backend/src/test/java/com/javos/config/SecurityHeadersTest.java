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

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HTTP security response headers added via Spring Security's
 * {@code .headers()} DSL in {@link SecurityConfig}.
 */
class SecurityHeadersTest extends BaseIntegrationTest {

    @Test
    void response_containsXContentTypeOptionsHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void response_containsXFrameOptionsHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void response_containsStrictTransportSecurityHeader() throws Exception {
        // HSTS is only emitted over HTTPS; use secure() to simulate an HTTPS request
        mockMvc.perform(get("/actuator/health").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    void response_containsContentSecurityPolicyHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void response_containsReferrerPolicyHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    void response_containsPermissionsPolicyHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Permissions-Policy"));
    }
}
