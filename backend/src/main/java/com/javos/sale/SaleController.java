package com.javos.sale;
import com.javos.sale.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Gerenciamento de vendas e itens de venda")
@SecurityRequirement(name = "Bearer Authentication")
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    @Operation(summary = "Listar vendas", description = "Retorna todas as vendas cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<SaleResponse>> findAll() { return ResponseEntity.ok(saleService.findAll()); }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar venda por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venda encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    })
    public ResponseEntity<SaleResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(saleService.findById(id)); }

    @PostMapping
    @Operation(summary = "Criar venda")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venda criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente ou vendedor não encontrado")
    })
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(saleService.create(request)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar venda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venda atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    })
    public ResponseEntity<SaleResponse> update(@PathVariable Long id, @Valid @RequestBody SaleRequest request) { return ResponseEntity.ok(saleService.update(id, request)); }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status da venda", description = "Atualiza o status de uma venda. O novo status é enviado como query parameter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    })
    public ResponseEntity<SaleResponse> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status da venda", required = true) @RequestParam SaleStatus status) {
        return ResponseEntity.ok(saleService.changeStatus(id, status));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar item à venda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda ou produto não encontrado")
    })
    public ResponseEntity<SaleResponse> addItem(@PathVariable Long id, @Valid @RequestBody SaleItemRequest request) { return ResponseEntity.ok(saleService.addItem(id, request)); }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remover item da venda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda ou item não encontrado")
    })
    public ResponseEntity<SaleResponse> removeItem(@PathVariable Long id, @PathVariable Long itemId) { return ResponseEntity.ok(saleService.removeItem(id, itemId)); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir venda")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Venda excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) { saleService.delete(id); return ResponseEntity.noContent().build(); }
}
