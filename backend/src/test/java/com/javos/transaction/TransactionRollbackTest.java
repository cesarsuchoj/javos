/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.transaction;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying transaction rollback behaviour for critical operations.
 *
 * <p>When a service-level operation fails partway through (e.g. a referenced entity is not found),
 * the {@code @Transactional} annotation must ensure no partial data is committed.</p>
 */
class TransactionRollbackTest extends BaseIntegrationTest {

    private long clientId;

    @BeforeEach
    void createClient() throws Exception {
        String result = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Rollback Test Client","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        clientId = objectMapper.readTree(result).get("id").asLong();
    }

    // ── Service Order rollback ────────────────────────────────────────────────

    @Test
    void createServiceOrder_withNonExistentClient_returns404AndNoPartialData() throws Exception {
        long nonExistentClientId = 999_999L;

        mockMvc.perform(post("/api/v1/service-orders")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "description": "Should not be persisted"
                                }
                                """.formatted(nonExistentClientId)))
                .andExpect(status().isNotFound());

        // List must not contain the rolled-back order
        String list = mockMvc.perform(get("/api/v1/service-orders")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Verify the failed order description is absent
        assertThat(list)
                .doesNotContain("Should not be persisted");
    }

    @Test
    void createServiceOrder_withNonExistentTechnician_returns404AndNoPartialData() throws Exception {
        long nonExistentTechId = 999_998L;

        mockMvc.perform(post("/api/v1/service-orders")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "technicianId": %d,
                                  "description": "Tech rollback test"
                                }
                                """.formatted(clientId, nonExistentTechId)))
                .andExpect(status().isNotFound());

        String list = mockMvc.perform(get("/api/v1/service-orders")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(list)
                .doesNotContain("Tech rollback test");
    }

    // ── Sale rollback ─────────────────────────────────────────────────────────

    @Test
    void createSale_withNonExistentClient_returns404AndNoPartialData() throws Exception {
        long nonExistentClientId = 999_997L;

        mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "status": "OPEN"
                                }
                                """.formatted(nonExistentClientId)))
                .andExpect(status().isNotFound());

        String list = mockMvc.perform(get("/api/v1/sales")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // The list must not contain any entry referencing the non-existent client
        assertThat(list)
                .doesNotContain("999997");
    }

    @Test
    void createSale_withNonExistentProduct_returns404AndSaleIsRolledBack() throws Exception {
        long nonExistentProductId = 999_996L;

        // Attempt to create a sale that includes a non-existent product in its items
        mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "status": "OPEN",
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 1,
                                      "unitPrice": 100.00
                                    }
                                  ]
                                }
                                """.formatted(clientId, nonExistentProductId)))
                .andExpect(status().isNotFound());

        // The sale must not appear in the list (the entire transaction was rolled back)
        String list = mockMvc.perform(get("/api/v1/sales")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Verify no sale for the non-existent product item was persisted
        assertThat(list)
                .doesNotContain("999996");
    }

    // ── Charge rollback ───────────────────────────────────────────────────────

    @Test
    void createCharge_withNonExistentClient_returns404AndNoPartialData() throws Exception {
        long nonExistentClientId = 999_995L;

        mockMvc.perform(post("/api/v1/charges")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "amount": 150.00,
                                  "status": "PENDING"
                                }
                                """.formatted(nonExistentClientId)))
                .andExpect(status().isNotFound());

        String list = mockMvc.perform(get("/api/v1/charges")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(list)
                .doesNotContain("999995");
    }

    // ── Data integrity violation ──────────────────────────────────────────────

    @Test
    void createClient_withDuplicateEmail_returns409Conflict() throws Exception {
        String email = "duplicate-tx-test@javos.test";
        String body = """
                {"name":"First Client","email":"%s","active":true}
                """.formatted(email);

        // Create first client successfully
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Second client with same email must be rejected as 409 Conflict
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Second Client","email":"%s","active":true}
                                """.formatted(email)))
                .andExpect(status().isConflict());
    }
}
