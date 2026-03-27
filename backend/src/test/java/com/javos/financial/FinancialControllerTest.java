/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.financial;

import com.javos.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/financial/** endpoints (categories, accounts, entries).
 */
class FinancialControllerTest extends BaseIntegrationTest {

    private static final String CATEGORIES_URL = "/api/v1/financial/categories";
    private static final String ACCOUNTS_URL = "/api/v1/financial/accounts";
    private static final String ENTRIES_URL = "/api/v1/financial/entries";

    private long categoryId;
    private long accountId;

    @BeforeEach
    void createCategoryAndAccount() throws Exception {
        String catResult = mockMvc.perform(post(CATEGORIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Category","type":"INCOME","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        categoryId = objectMapper.readTree(catResult).get("id").asLong();

        String accResult = mockMvc.perform(post(ACCOUNTS_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Account","type":"CHECKING","balance":1000.00,"active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        accountId = objectMapper.readTree(accResult).get("id").asLong();
    }

    // ── Categories ────────────────────────────────────────────

    @Test
    void findAllCategories_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(CATEGORIES_URL).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findAllCategories_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(CATEGORIES_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_withValidData_returns201() throws Exception {
        mockMvc.perform(post(CATEGORIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Salary","type":"INCOME","active":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void createCategory_withMissingType_returns400() throws Exception {
        mockMvc.perform(post(CATEGORIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"No Type","active":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findCategoryById_existingId_returns200() throws Exception {
        mockMvc.perform(get(CATEGORIES_URL + "/" + categoryId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId));
    }

    @Test
    void findCategoryById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(CATEGORIES_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_existingId_returns200() throws Exception {
        mockMvc.perform(put(CATEGORIES_URL + "/" + categoryId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated Category","type":"INCOME","active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void deleteCategory_existingId_returns204() throws Exception {
        String result = mockMvc.perform(post(CATEGORIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"To Delete Cat","type":"EXPENSE","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(CATEGORIES_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());
    }

    // ── Accounts ─────────────────────────────────────────────

    @Test
    void findAllAccounts_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(ACCOUNTS_URL).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createAccount_withValidData_returns201() throws Exception {
        mockMvc.perform(post(ACCOUNTS_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Savings Account","type":"SAVINGS","balance":500.00,"active":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.type").value("SAVINGS"));
    }

    @Test
    void createAccount_withMissingType_returns400() throws Exception {
        mockMvc.perform(post(ACCOUNTS_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"No Type Account","active":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAccountById_existingId_returns200() throws Exception {
        mockMvc.perform(get(ACCOUNTS_URL + "/" + accountId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void findAccountById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(ACCOUNTS_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_existingId_returns200() throws Exception {
        mockMvc.perform(put(ACCOUNTS_URL + "/" + accountId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated Account","type":"CHECKING","balance":2000.00,"active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account"));
    }

    @Test
    void deleteAccount_existingId_returns204() throws Exception {
        String result = mockMvc.perform(post(ACCOUNTS_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"To Delete Acc","type":"CASH","active":true}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(ACCOUNTS_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());
    }

    // ── Financial Entries ─────────────────────────────────────

    @Test
    void findAllEntries_authenticated_returns200AndList() throws Exception {
        mockMvc.perform(get(ENTRIES_URL).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createEntry_withValidData_returns201() throws Exception {
        mockMvc.perform(post(ENTRIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Monthly salary",
                                  "type": "INCOME",
                                  "amount": 3000.00,
                                  "dueDate": "2025-12-31",
                                  "paid": false,
                                  "categoryId": %d,
                                  "accountId": %d
                                }
                                """.formatted(categoryId, accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Monthly salary"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(3000.00));
    }

    @Test
    void createEntry_withMissingDescription_returns400() throws Exception {
        mockMvc.perform(post(ENTRIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"INCOME","amount":100.00}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findEntryById_existingId_returns200() throws Exception {
        String result = mockMvc.perform(post(ENTRIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Entry to find",
                                  "type": "EXPENSE",
                                  "amount": 150.00
                                }
                                """))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(get(ENTRIES_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void findEntryById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(ENTRIES_URL + "/999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEntry_existingId_returns200() throws Exception {
        String result = mockMvc.perform(post(ENTRIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Entry to update","type":"EXPENSE","amount":100.00}
                                """))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(put(ENTRIES_URL + "/" + id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Updated Entry","type":"EXPENSE","amount":200.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Entry"))
                .andExpect(jsonPath("$.amount").value(200.00));
    }

    @Test
    void deleteEntry_existingId_returns204() throws Exception {
        String result = mockMvc.perform(post(ENTRIES_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Entry to delete","type":"EXPENSE","amount":50.00}
                                """))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(ENTRIES_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ENTRIES_URL + "/" + id).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }
}
