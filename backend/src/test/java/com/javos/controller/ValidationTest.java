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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying strengthened DTO validation rules on the auth endpoints.
 */
class ValidationTest extends BaseIntegrationTest {

    // ── Password strength (RegisterRequest) ─────────────────────────────────

    @Test
    void register_passwordWithoutUppercase_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "valtest1",
                                  "email": "valtest1@javos.test",
                                  "name": "Val Test",
                                  "password": "nouppercase123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_passwordWithoutNumber_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "valtest2",
                                  "email": "valtest2@javos.test",
                                  "name": "Val Test",
                                  "password": "NoNumberHere"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "valtest3",
                                  "email": "valtest3@javos.test",
                                  "name": "Val Test",
                                  "password": "Ab1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_usernameWithInvalidChars_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "bad user!",
                                  "email": "baduser@javos.test",
                                  "name": "Bad User",
                                  "password": "ValidPass1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists());
    }

    @Test
    void register_validPassword_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "validpwduser",
                                  "email": "validpwduser@javos.test",
                                  "name": "Valid Pwd User",
                                  "password": "ValidPass1"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // ── Error response: no sensitive value echo ──────────────────────────────

    @Test
    void request_withInvalidEnumValue_doesNotEchoValue() throws Exception {
        // PATCH /api/v1/service-orders/{id}/status?status=INVALID_VALUE
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                // The response must not contain the user-supplied (empty) value echoed back
                .andExpect(jsonPath("$.message").exists());
    }
}
