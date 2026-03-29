/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base class for REST endpoint integration tests.
 * Uses in-memory SQLite (test profile) and authenticates as admin before each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String adminToken;

    /**
     * Registers and logs in a test admin user before each test to obtain a valid JWT.
     * Uses the /api/v1/auth/register + /api/v1/auth/login flow.
     */
    @BeforeEach
    void authenticate() throws Exception {
        // Register admin user (ignore conflict if already exists from a previous test)
        String registerBody = """
                {
                  "username": "testadmin",
                  "email": "testadmin@javos.test",
                  "name": "Test Admin",
                  "password": "AdminPass123"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody));

        // Log in to get token
        String loginBody = """
                {"username":"testadmin","password":"AdminPass123"}
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn();

        adminToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
    }

    protected String bearerToken() {
        return "Bearer " + adminToken;
    }
}
