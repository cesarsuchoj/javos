package com.javos.audit;
import com.javos.audit.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Consulta de logs de auditoria do sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Listar todos os logs de auditoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<AuditLogResponse>> findAll() { return ResponseEntity.ok(auditService.findAll()); }

    @GetMapping("/user/{username}")
    @Operation(summary = "Listar logs por usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<AuditLogResponse>> findByUsername(@PathVariable String username) { return ResponseEntity.ok(auditService.findByUsername(username)); }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Listar logs por entidade", description = "Retorna logs de auditoria filtrados por tipo de entidade (ex: CLIENT, PRODUCT) e ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<AuditLogResponse>> findByEntity(@PathVariable String entityType, @PathVariable Long entityId) { return ResponseEntity.ok(auditService.findByEntity(entityType, entityId)); }
}
