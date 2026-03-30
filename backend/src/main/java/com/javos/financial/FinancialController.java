package com.javos.financial;
import com.javos.financial.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/financial")
@RequiredArgsConstructor
@Tag(name = "Financeiro", description = "Gerenciamento de categorias, contas e lançamentos financeiros")
@SecurityRequirement(name = "Bearer Authentication")
public class FinancialController {
    private final FinancialService financialService;

    // --- Categorias ---

    @GetMapping("/categories")
    @Operation(summary = "Listar categorias financeiras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<CategoryResponse>> findAllCategories() { return ResponseEntity.ok(financialService.findAllCategories()); }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoryResponse> findCategoryById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findCategoryById(id)); }

    @PostMapping("/categories")
    @Operation(summary = "Criar categoria financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createCategory(request)); }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Atualizar categoria financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return ResponseEntity.ok(financialService.updateCategory(id, request)); }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Excluir categoria financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Categoria possui lançamentos associados")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) { financialService.deleteCategory(id); return ResponseEntity.noContent().build(); }

    // --- Contas ---

    @GetMapping("/accounts")
    @Operation(summary = "Listar contas financeiras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<AccountResponse>> findAllAccounts() { return ResponseEntity.ok(financialService.findAllAccounts()); }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Buscar conta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponse> findAccountById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findAccountById(id)); }

    @PostMapping("/accounts")
    @Operation(summary = "Criar conta financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createAccount(request)); }

    @PutMapping("/accounts/{id}")
    @Operation(summary = "Atualizar conta financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountRequest request) { return ResponseEntity.ok(financialService.updateAccount(id, request)); }

    @DeleteMapping("/accounts/{id}")
    @Operation(summary = "Excluir conta financeira")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) { financialService.deleteAccount(id); return ResponseEntity.noContent().build(); }

    // --- Lançamentos ---

    @GetMapping("/entries")
    @Operation(summary = "Listar lançamentos financeiros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<FinancialEntryResponse>> findAllEntries() { return ResponseEntity.ok(financialService.findAllEntries()); }

    @GetMapping("/entries/{id}")
    @Operation(summary = "Buscar lançamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lançamento encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
    })
    public ResponseEntity<FinancialEntryResponse> findEntryById(@PathVariable Long id) { return ResponseEntity.ok(financialService.findEntryById(id)); }

    @PostMapping("/entries")
    @Operation(summary = "Criar lançamento financeiro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lançamento criado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<FinancialEntryResponse> createEntry(@Valid @RequestBody FinancialEntryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createEntry(request)); }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Atualizar lançamento financeiro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lançamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
    })
    public ResponseEntity<FinancialEntryResponse> updateEntry(@PathVariable Long id, @Valid @RequestBody FinancialEntryRequest request) { return ResponseEntity.ok(financialService.updateEntry(id, request)); }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Excluir lançamento financeiro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lançamento excluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
    })
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) { financialService.deleteEntry(id); return ResponseEntity.noContent().build(); }
}
