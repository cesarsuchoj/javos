package com.javos.financial;
import com.javos.financial.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/financial")
@RequiredArgsConstructor
public class FinancialController {
    private final FinancialService financialService;
    @GetMapping("/categories") public ResponseEntity<List<CategoryResponse>> findAllCategories() { return ResponseEntity.ok(financialService.findAllCategories()); }
    @GetMapping("/categories/{id}") public ResponseEntity<CategoryResponse> findCategoryById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findCategoryById(id)); }
    @PostMapping("/categories") public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createCategory(request)); }
    @PutMapping("/categories/{id}") public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return ResponseEntity.ok(financialService.updateCategory(id, request)); }
    @DeleteMapping("/categories/{id}") public ResponseEntity<Void> deleteCategory(@PathVariable Long id) { financialService.deleteCategory(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/accounts") public ResponseEntity<List<AccountResponse>> findAllAccounts() { return ResponseEntity.ok(financialService.findAllAccounts()); }
    @GetMapping("/accounts/{id}") public ResponseEntity<AccountResponse> findAccountById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findAccountById(id)); }
    @PostMapping("/accounts") public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createAccount(request)); }
    @PutMapping("/accounts/{id}") public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountRequest request) { return ResponseEntity.ok(financialService.updateAccount(id, request)); }
    @DeleteMapping("/accounts/{id}") public ResponseEntity<Void> deleteAccount(@PathVariable Long id) { financialService.deleteAccount(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/entries") public ResponseEntity<List<FinancialEntryResponse>> findAllEntries() { return ResponseEntity.ok(financialService.findAllEntries()); }
    @GetMapping("/entries/{id}") public ResponseEntity<FinancialEntryResponse> findEntryById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findEntryById(id)); }
    @PostMapping("/entries") public ResponseEntity<FinancialEntryResponse> createEntry(@Valid @RequestBody FinancialEntryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createEntry(request)); }
    @PutMapping("/entries/{id}") public ResponseEntity<FinancialEntryResponse> updateEntry(@PathVariable Long id, @Valid @RequestBody FinancialEntryRequest request) { return ResponseEntity.ok(financialService.updateEntry(id, request)); }
    @DeleteMapping("/entries/{id}") public ResponseEntity<Void> deleteEntry(@PathVariable Long id) { financialService.deleteEntry(id); return ResponseEntity.noContent().build(); }
}
