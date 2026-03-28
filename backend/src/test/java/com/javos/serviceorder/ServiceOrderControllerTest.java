/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.serviceorder;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/service-orders endpoints.
 */
class ServiceOrderControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/service-orders";

    private long clientId;

    @BeforeEach
    void createClient() throws Exception {
        String result = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"OS Client","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        clientId = objectMapper.readTree(result).get("id").asLong();
    }

    private String serviceOrderJson() {
        return """
                {
                  "clientId": %d,
                  "description": "Notebook with screen issue",
                  "priority": "HIGH"
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
                        .content(serviceOrderJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Notebook with screen issue"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void create_withMissingClientId_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"No client"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_existingOrder_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void findById_nonExistingOrder_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatus_validStatus_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        // Status must be sent as a query parameter, not a JSON body
        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void changeStatus_invalidStatus_returns400() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .header("Authorization", bearerToken())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_existingOrder_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "description": "Updated description",
                                  "priority": "NORMAL"
                                }
                                """.formatted(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void delete_existingOrder_returns204() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        // Soft-delete: order remains in DB with status=CANCELLED; GET still returns 200
        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getNotes_existingOrder_returns200AndList() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id + "/notes").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void addNote_validNote_returns201() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceOrderJson()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(post(BASE_URL + "/" + id + "/notes")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Customer called to check status"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Customer called to check status"));
    }
}
