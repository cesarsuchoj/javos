/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.systemconfig;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/system-config endpoints.
 */
class SystemConfigControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/system-config";

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
    void findByKey_existingKey_returns200() throws Exception {
        // Get all configs first to find an existing key
        String allResult = mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andReturn().getResponse().getContentAsString();

        // Only proceed if at least one config entry exists (seeded by V12 migration)
        if (objectMapper.readTree(allResult).size() > 0) {
            String firstKey = objectMapper.readTree(allResult).get(0).get("key").asText();

            mockMvc.perform(get(BASE_URL + "/" + firstKey).header("Authorization", bearerToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.key").value(firstKey));
        }
    }

    @Test
    void findByKey_nonExistingKey_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/nonexistent.key.xyz").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingKey_returns200() throws Exception {
        // Get all configs to find an existing key
        String allResult = mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andReturn().getResponse().getContentAsString();

        if (objectMapper.readTree(allResult).size() > 0) {
            String firstKey = objectMapper.readTree(allResult).get(0).get("key").asText();
            String currentValue = objectMapper.readTree(allResult).get(0).get("value").asText();

            mockMvc.perform(put(BASE_URL + "/" + firstKey)
                            .header("Authorization", bearerToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"value\":\"" + currentValue + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.key").value(firstKey));
        }
    }
}
