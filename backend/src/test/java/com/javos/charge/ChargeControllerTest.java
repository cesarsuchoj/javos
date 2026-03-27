/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.charge;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/charges endpoints.
 */
class ChargeControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/charges";

    private String chargeJson() {
        return """
                {
                  "amount": 250.00,
                  "dueDate": "2025-12-31",
                  "status": "PENDING",
                  "method": "PIX",
                  "notes": "Test charge"
                }
                """;
    }

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
    void create_withValidData_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(250.00));
    }

    @Test
    void create_withMissingAmount_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"Missing amount"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_existingCharge_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void findById_nonExistingCharge_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_validStatus_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        // Status must be sent as a query parameter, not a JSON body
        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingCharge_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500.00,"notes":"Updated charge"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void delete_existingCharge_returns204() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }
}
