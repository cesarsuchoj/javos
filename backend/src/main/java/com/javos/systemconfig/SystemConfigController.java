package com.javos.systemconfig;
import com.javos.systemconfig.dto.SystemConfigResponse;
import com.javos.systemconfig.dto.SystemConfigUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
@Tag(name = "Configurações do Sistema", description = "Leitura e atualização de configurações globais da aplicação")
@SecurityRequirement(name = "Bearer Authentication")
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "Listar configurações", description = "Retorna todas as configurações do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<SystemConfigResponse>> findAll() { return ResponseEntity.ok(systemConfigService.findAll()); }

    @GetMapping("/{key}")
    @Operation(summary = "Buscar configuração por chave")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    public ResponseEntity<SystemConfigResponse> findByKey(@PathVariable String key) { return ResponseEntity.ok(systemConfigService.findByKey(key)); }

    @PutMapping("/{key}")
    @Operation(summary = "Atualizar configuração", description = "Atualiza o valor de uma configuração existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    public ResponseEntity<SystemConfigResponse> update(@PathVariable String key, @Valid @RequestBody SystemConfigUpdateRequest request) { return ResponseEntity.ok(systemConfigService.update(key, request)); }
}
