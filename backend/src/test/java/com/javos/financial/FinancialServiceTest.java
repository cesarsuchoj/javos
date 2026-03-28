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

import com.javos.exception.ResourceNotFoundException;
import com.javos.financial.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FinancialEntryRepository entryRepository;

    @InjectMocks
    private FinancialService financialService;

    private Category category;
    private Account account;
    private FinancialEntry entry;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Receita de Serviços")
                .type(CategoryType.INCOME)
                .description("Serviços prestados")
                .active(true)
                .build();

        account = Account.builder()
                .id(1L)
                .name("Conta Corrente")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .active(true)
                .build();

        entry = FinancialEntry.builder()
                .id(1L)
                .description("Pagamento serviço OS001")
                .type(EntryType.INCOME)
                .amount(new BigDecimal("500.00"))
                .dueDate(LocalDate.of(2025, 6, 30))
                .paid(false)
                .build();
    }

    // ── Category Tests ────────────────────────────────────────────────────────

    @Test
    void findAllCategories_returnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = financialService.findAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Receita de Serviços");
        assertThat(result.get(0).getType()).isEqualTo(CategoryType.INCOME);
        verify(categoryRepository).findAll();
    }

    @Test
    void findAllCategories_emptyRepository_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = financialService.findAllCategories();

        assertThat(result).isEmpty();
    }

    @Test
    void findCategoryById_existingId_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse result = financialService.findCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Receita de Serviços");
    }

    @Test
    void findCategoryById_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.findCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createCategory_validRequest_savesAndReturnsCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Despesas Fixas");
        request.setType(CategoryType.EXPENSE);
        request.setDescription("Despesas mensais fixas");
        request.setActive(true);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = financialService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Receita de Serviços");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_existingCategory_updatesAndReturns() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Atualizado");
        request.setType(CategoryType.EXPENSE);
        request.setActive(false);

        Category updated = Category.builder().id(1L).name("Atualizado").type(CategoryType.EXPENSE).active(false).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryResponse result = financialService.updateCategory(1L, request);

        assertThat(result.getName()).isEqualTo("Atualizado");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_nonExistingCategory_throwsResourceNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.updateCategory(999L, new CategoryRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCategory_existingCategory_deletesCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        financialService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_nonExistingCategory_throwsResourceNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Account Tests ─────────────────────────────────────────────────────────

    @Test
    void findAllAccounts_returnsAllAccounts() {
        when(accountRepository.findAll()).thenReturn(List.of(account));

        List<AccountResponse> result = financialService.findAllAccounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Conta Corrente");
        assertThat(result.get(0).getType()).isEqualTo(AccountType.CHECKING);
        verify(accountRepository).findAll();
    }

    @Test
    void findAccountById_existingId_returnsAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse result = financialService.findAccountById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void findAccountById_nonExistingId_throwsResourceNotFoundException() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.findAccountById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createAccount_withNullBalance_defaultsToZero() {
        AccountRequest request = new AccountRequest();
        request.setName("Conta Poupança");
        request.setType(AccountType.SAVINGS);
        request.setBalance(null);
        request.setActive(true);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        financialService.createAccount(request);

        verify(accountRepository).save(argThat(a -> a.getBalance().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void createAccount_validRequest_savesAndReturnsAccount() {
        AccountRequest request = new AccountRequest();
        request.setName("Conta Corrente");
        request.setType(AccountType.CHECKING);
        request.setBalance(new BigDecimal("500.00"));
        request.setActive(true);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse result = financialService.createAccount(request);

        assertThat(result.getName()).isEqualTo("Conta Corrente");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_existingAccount_updatesAndReturns() {
        AccountRequest request = new AccountRequest();
        request.setName("Conta Atualizada");
        request.setType(AccountType.SAVINGS);
        request.setBalance(new BigDecimal("2000.00"));
        request.setActive(true);

        Account updated = Account.builder().id(1L).name("Conta Atualizada").type(AccountType.SAVINGS).balance(new BigDecimal("2000.00")).active(true).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(updated);

        AccountResponse result = financialService.updateAccount(1L, request);

        assertThat(result.getName()).isEqualTo("Conta Atualizada");
        assertThat(result.getBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    void updateAccount_nonExistingAccount_throwsResourceNotFoundException() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.updateAccount(999L, new AccountRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAccount_existingAccount_deletesAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        financialService.deleteAccount(1L);

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteAccount_nonExistingAccount_throwsResourceNotFoundException() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.deleteAccount(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Financial Entry Tests ─────────────────────────────────────────────────

    @Test
    void findAllEntries_returnsAllEntries() {
        when(entryRepository.findAll()).thenReturn(List.of(entry));

        List<FinancialEntryResponse> result = financialService.findAllEntries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Pagamento serviço OS001");
        assertThat(result.get(0).getType()).isEqualTo(EntryType.INCOME);
        verify(entryRepository).findAll();
    }

    @Test
    void findEntryById_existingId_returnsEntry() {
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        FinancialEntryResponse result = financialService.findEntryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void findEntryById_nonExistingId_throwsResourceNotFoundException() {
        when(entryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.findEntryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createEntry_withoutCategoryAndAccount_savesEntry() {
        FinancialEntryRequest request = new FinancialEntryRequest();
        request.setDescription("Pagamento serviço OS001");
        request.setType(EntryType.INCOME);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaid(false);

        when(entryRepository.save(any(FinancialEntry.class))).thenReturn(entry);

        FinancialEntryResponse result = financialService.createEntry(request);

        assertThat(result.getDescription()).isEqualTo("Pagamento serviço OS001");
        verify(entryRepository).save(any(FinancialEntry.class));
        verifyNoInteractions(categoryRepository, accountRepository);
    }

    @Test
    void createEntry_withCategoryAndAccount_loadsAndSaves() {
        FinancialEntryRequest request = new FinancialEntryRequest();
        request.setDescription("Entry with refs");
        request.setType(EntryType.INCOME);
        request.setAmount(new BigDecimal("200.00"));
        request.setCategoryId(1L);
        request.setAccountId(1L);

        FinancialEntry entryWithRefs = FinancialEntry.builder()
                .id(2L)
                .description("Entry with refs")
                .type(EntryType.INCOME)
                .amount(new BigDecimal("200.00"))
                .category(category)
                .account(account)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(entryRepository.save(any(FinancialEntry.class))).thenReturn(entryWithRefs);

        FinancialEntryResponse result = financialService.createEntry(request);

        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getAccountId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
        verify(accountRepository).findById(1L);
    }

    @Test
    void createEntry_withInvalidCategoryId_throwsResourceNotFoundException() {
        FinancialEntryRequest request = new FinancialEntryRequest();
        request.setDescription("Entry");
        request.setType(EntryType.INCOME);
        request.setAmount(new BigDecimal("100.00"));
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.createEntry(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateEntry_existingEntry_updatesAndReturns() {
        FinancialEntryRequest request = new FinancialEntryRequest();
        request.setDescription("Atualizado");
        request.setType(EntryType.EXPENSE);
        request.setAmount(new BigDecimal("300.00"));
        request.setPaid(true);

        FinancialEntry updated = FinancialEntry.builder()
                .id(1L)
                .description("Atualizado")
                .type(EntryType.EXPENSE)
                .amount(new BigDecimal("300.00"))
                .paid(true)
                .build();

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(entryRepository.save(any(FinancialEntry.class))).thenReturn(updated);

        FinancialEntryResponse result = financialService.updateEntry(1L, request);

        assertThat(result.getDescription()).isEqualTo("Atualizado");
        assertThat(result.isPaid()).isTrue();
    }

    @Test
    void updateEntry_nonExistingEntry_throwsResourceNotFoundException() {
        when(entryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.updateEntry(999L, new FinancialEntryRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteEntry_existingEntry_deletesEntry() {
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        financialService.deleteEntry(1L);

        verify(entryRepository).delete(entry);
    }

    @Test
    void deleteEntry_nonExistingEntry_throwsResourceNotFoundException() {
        when(entryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.deleteEntry(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findEntryById_responseHasNullCategoryAndAccountWhenNotLinked() {
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        FinancialEntryResponse result = financialService.findEntryById(1L);

        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getAccountId()).isNull();
    }
}
