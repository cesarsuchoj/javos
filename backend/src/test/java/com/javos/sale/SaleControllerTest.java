/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.sale;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/sales endpoints.
 */
class SaleControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/sales";

    private long clientId;

    @BeforeEach
    void createClient() throws Exception {
        String result = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sale Client","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        clientId = objectMapper.readTree(result).get("id").asLong();
    }

    private String saleJson() {
        return """
                {
                  "clientId": %d,
                  "status": "OPEN",
                  "discount": 0
                }
                """.formatted(clientId);
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
                        .content(saleJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.saleNumber").isString())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void create_withMissingClientId_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"OPEN"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_existingSale_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void findById_nonExistingSale_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatus_validStatus_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void changeStatus_invalidStatus_returns400() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingSale_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "status": "CONFIRMED",
                                  "notes": "Updated notes",
                                  "discount": 10.00
                                }
                                """.formatted(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void delete_existingSale_returns204AndSaleIsCancelled() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        // Sale is soft-deleted (status set to CANCELLED), so it still exists
        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
