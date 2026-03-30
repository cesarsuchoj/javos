package com.javos.serviceorder;
import com.javos.serviceorder.dto.*;
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
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Gerenciamento de ordens de serviço (OS)")
@SecurityRequirement(name = "Bearer Authentication")
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;

    @GetMapping
    @Operation(summary = "Listar ordens de serviço", description = "Retorna todas as ordens de serviço cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<ServiceOrderResponse>> findAll() { return ResponseEntity.ok(serviceOrderService.findAll()); }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ordem de serviço por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OS encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<ServiceOrderResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(serviceOrderService.findById(id)); }

    @PostMapping
    @Operation(summary = "Criar ordem de serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "OS criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente ou técnico não encontrado")
    })
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.create(request)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ordem de serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OS atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<ServiceOrderResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceOrderRequest request) { return ResponseEntity.ok(serviceOrderService.update(id, request)); }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status da OS", description = "Atualiza o status de uma ordem de serviço. O novo status é enviado como query parameter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<ServiceOrderResponse> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status da OS", required = true) @RequestParam ServiceOrderStatus status) {
        return ResponseEntity.ok(serviceOrderService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ordem de serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "OS excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) { serviceOrderService.delete(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/{id}/notes")
    @Operation(summary = "Listar notas da OS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notas retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<List<OsNoteResponse>> getNotes(@PathVariable Long id) { return ResponseEntity.ok(serviceOrderService.getNotes(id)); }

    @PostMapping("/{id}/notes")
    @Operation(summary = "Adicionar nota à OS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nota adicionada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "OS não encontrada")
    })
    public ResponseEntity<OsNoteResponse> addNote(@PathVariable Long id, @Valid @RequestBody OsNoteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.addNote(id, request)); }
}
