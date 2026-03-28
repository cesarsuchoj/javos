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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/users endpoints.
 * Authenticates as the pre-created admin user (ROLE_ADMIN) to access
 * endpoints protected by @PreAuthorize("hasRole('ADMIN')").
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String adminToken;

    /**
     * Authenticates as the pre-created admin user (username: admin, password: admin123, role: ROLE_ADMIN).
     * This user is created by DataInitializer and has ROLE_ADMIN, allowing access to user management.
     */
    @BeforeEach
    void authenticate() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andReturn();

        adminToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
    }

    protected String bearerToken() {
        return "Bearer " + adminToken;
    }

    @Test
    void findAll_asAdmin_returns200AndList() throws Exception {
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
    void findById_existingUser_returns200() throws Exception {
        String listResult = mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(listResult).get(0).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + userId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").isString());
    }

    @Test
    void findById_nonExistingUser_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_existingUser_returns200() throws Exception {
        String listResult = mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(listResult).get(0).get("id").asLong();
        String username = objectMapper.readTree(listResult).get(0).get("username").asText();
        String email = objectMapper.readTree(listResult).get(0).get("email").asText();

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "name": "Updated Name",
                                  "email": "%s",
                                  "active": true
                                }
                                """.formatted(username, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void update_nonExistingUser_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/999999")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "nobody",
                                  "name": "Nobody",
                                  "email": "nobody@javos.test",
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingUser_returns204() throws Exception {
        // Register a user to delete
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "todelete_user",
                          "email": "todelete_user@javos.test",
                          "name": "To Delete",
                          "password": "password123"
                        }
                        """));

        String listResult = mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken()))
                .andReturn().getResponse().getContentAsString();

        Long deleteId = null;
        for (var node : objectMapper.readTree(listResult)) {
            if ("todelete_user".equals(node.get("username").asText())) {
                deleteId = node.get("id").asLong();
                break;
            }
        }

        if (deleteId != null) {
            mockMvc.perform(delete(BASE_URL + "/" + deleteId)
                            .header("Authorization", bearerToken()))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void delete_nonExistingUser_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }
}
