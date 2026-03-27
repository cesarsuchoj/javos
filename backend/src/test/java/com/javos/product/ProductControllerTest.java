/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.product;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/products endpoints.
 */
class ProductControllerTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/products";

    private String productJson(String name, String code) {
        return """
                {
                  "name": "%s",
                  "code": "%s",
                  "type": "PRODUCT",
                  "price": 99.99,
                  "active": true
                }
                """.formatted(name, code);
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
                        .content(productJson("Laptop Pro", "LAPTP001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.type").value("PRODUCT"));
    }

    @Test
    void create_withMissingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"incomplete","active":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_existingProduct_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Mouse Wireless", "MOUSE001")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse Wireless"));
    }

    @Test
    void findById_nonExistingProduct_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByCode_existingCode_returns200() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Keyboard USB", "KB-USB-002")));

        mockMvc.perform(get(BASE_URL + "/code/KB-USB-002").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("KB-USB-002"));
    }

    @Test
    void update_existingProduct_returns200() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Old Name", "UPD-001")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("New Name", "UPD-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void delete_existingProduct_returns204() throws Exception {
        String result = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Product To Delete", "DEL-001")))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_searchByName_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL).param("name", "Mouse")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
