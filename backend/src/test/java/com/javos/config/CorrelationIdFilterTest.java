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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CorrelationIdFilter.
 */
class CorrelationIdFilterTest extends BaseIntegrationTest {

    @Test
    void request_withoutCorrelationId_responseContainsGeneratedCorrelationId() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testadmin","password":"AdminPass123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void request_withCorrelationId_responseEchoesTheSameId() throws Exception {
        String correlationId = "test-correlation-id-abc123";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                        .content("""
                                {"username":"testadmin","password":"AdminPass123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId));
    }

    @Test
    void actuatorHealth_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
