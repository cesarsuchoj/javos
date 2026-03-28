/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.audit;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/audit endpoints.
 */
class AuditControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/audit";

    @Test
    void findAll_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findByUsername_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/testadmin")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findByUsername_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/testadmin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findByEntity_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/entity/Client/1")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findByEntity_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/entity/Client/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findByUsername_nonExistingUser_returns200WithEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/nobody")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findByEntity_nonExistingEntity_returns200WithEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/entity/NonExistent/999999")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
