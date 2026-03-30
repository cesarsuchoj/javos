package com.javos.charge;
import com.javos.charge.dto.ChargeRequest;
import com.javos.charge.dto.ChargeResponse;
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
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
@Tag(name = "Cobranças", description = "Gerenciamento de cobranças e pagamentos")
@SecurityRequirement(name = "Bearer Authentication")
public class ChargeController {
    private final ChargeService chargeService;

    @GetMapping
    @Operation(summary = "Listar cobranças", description = "Retorna todas as cobranças cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<ChargeResponse>> findAll() { return ResponseEntity.ok(chargeService.findAll()); }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cobrança por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada")
    })
    public ResponseEntity<ChargeResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(chargeService.findById(id)); }

    @PostMapping
    @Operation(summary = "Criar cobrança")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cobrança criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<ChargeResponse> create(@Valid @RequestBody ChargeRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(chargeService.create(request)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cobrança")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada")
    })
    public ResponseEntity<ChargeResponse> update(@PathVariable Long id, @Valid @RequestBody ChargeRequest request) { return ResponseEntity.ok(chargeService.update(id, request)); }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status da cobrança", description = "Altera o status de uma cobrança. O novo status é enviado como query parameter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada")
    })
    public ResponseEntity<ChargeResponse> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status da cobrança", required = true) @RequestParam ChargeStatus status) {
        return ResponseEntity.ok(chargeService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cobrança")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cobrança excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) { chargeService.delete(id); return ResponseEntity.noContent().build(); }
}
