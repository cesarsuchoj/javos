package com.javos.financial;

import com.javos.exception.ResourceNotFoundException;
import com.javos.financial.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final FinancialEntryRepository entryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(Long id) {
        return toCategoryResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.delete(getCategory(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAllAccounts() {
        return accountRepository.findAll().stream().map(this::toAccountResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse findAccountById(Long id) {
        return toAccountResponse(getAccount(id));
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = Account.builder()
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .active(request.isActive())
                .build();
        return toAccountResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        Account account = getAccount(id);
        account.setName(request.getName());
        account.setType(request.getType());
        if (request.getBalance() != null) account.setBalance(request.getBalance());
        account.setActive(request.isActive());
        return toAccountResponse(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.delete(getAccount(id));
    }

    @Transactional(readOnly = true)
    public List<FinancialEntryResponse> findAllEntries() {
        return entryRepository.findAll().stream().map(this::toEntryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FinancialEntryResponse findEntryById(Long id) {
        return toEntryResponse(getEntry(id));
    }

    @Transactional
    public FinancialEntryResponse createEntry(FinancialEntryRequest request) {
        Category category = request.getCategoryId() != null ? getCategory(request.getCategoryId()) : null;
        Account account = request.getAccountId() != null ? getAccount(request.getAccountId()) : null;
        FinancialEntry entry = FinancialEntry.builder()
                .description(request.getDescription())
                .type(request.getType())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .paymentDate(request.getPaymentDate())
                .paid(request.isPaid())
                .category(category)
                .account(account)
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .notes(request.getNotes())
                .build();
        return toEntryResponse(entryRepository.save(entry));
    }

    @Transactional
    public FinancialEntryResponse updateEntry(Long id, FinancialEntryRequest request) {
        FinancialEntry entry = getEntry(id);
        entry.setDescription(request.getDescription());
        entry.setType(request.getType());
        entry.setAmount(request.getAmount());
        entry.setDueDate(request.getDueDate());
        entry.setPaymentDate(request.getPaymentDate());
        entry.setPaid(request.isPaid());
        if (request.getCategoryId() != null) entry.setCategory(getCategory(request.getCategoryId()));
        if (request.getAccountId() != null) entry.setAccount(getAccount(request.getAccountId()));
        entry.setReferenceId(request.getReferenceId());
        entry.setReferenceType(request.getReferenceType());
        entry.setNotes(request.getNotes());
        return toEntryResponse(entryRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long id) {
        entryRepository.delete(getEntry(id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private FinancialEntry getEntry(Long id) {
        return entryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Financial entry not found: " + id));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .description(category.getDescription())
                .active(category.isActive())
                .build();
    }

    private AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .active(account.isActive())
                .build();
    }

    private FinancialEntryResponse toEntryResponse(FinancialEntry entry) {
        return FinancialEntryResponse.builder()
                .id(entry.getId())
                .description(entry.getDescription())
                .type(entry.getType())
                .amount(entry.getAmount())
                .dueDate(entry.getDueDate())
                .paymentDate(entry.getPaymentDate())
                .paid(entry.isPaid())
                .categoryId(entry.getCategory() != null ? entry.getCategory().getId() : null)
                .categoryName(entry.getCategory() != null ? entry.getCategory().getName() : null)
                .accountId(entry.getAccount() != null ? entry.getAccount().getId() : null)
                .accountName(entry.getAccount() != null ? entry.getAccount().getName() : null)
                .referenceId(entry.getReferenceId())
                .referenceType(entry.getReferenceType())
                .notes(entry.getNotes())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
