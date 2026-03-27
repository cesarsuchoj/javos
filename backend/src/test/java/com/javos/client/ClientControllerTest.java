/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.client;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/clients endpoints.
 */
class ClientControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/clients";

    private String clientJson(String name) {
        return """
                {
                  "name": "%s",
                  "email": "client@example.com",
                  "phone": "11999990000",
                  "document": "12345678901",
                  "active": true
                }
                """.formatted(name);
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
    void create_withValidData_returns201AndCreatedClient() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("João Silva")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void create_withMissingName_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@test.com","active":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_existingId_returns200() throws Exception {
        // Create a client first
        String createResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Maria Souza")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createResult).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    void findById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingClient_returns200WithUpdatedData() throws Exception {
        String createResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Pedro Santos")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createResult).get("id").asLong();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Pedro Santos Updated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro Santos Updated"));
    }

    @Test
    void delete_existingClient_returns204() throws Exception {
        String createResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Client To Delete")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createResult).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_searchByName_returnsFilteredList() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson("Unique Name XYZ")));

        mockMvc.perform(get(BASE_URL).param("name", "Unique Name XYZ")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
