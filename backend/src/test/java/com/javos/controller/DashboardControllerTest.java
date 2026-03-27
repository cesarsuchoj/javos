/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.controller;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GET /api/v1/dashboard/summary endpoint.
 */
class DashboardControllerTest extends BaseIntegrationTest {

    @Test
    void getSummary_authenticated_returns200WithExpectedFields() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.loggedUser").value("testadmin"))
                .andExpect(jsonPath("$.version").isNotEmpty());
    }

    @Test
    void getSummary_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
