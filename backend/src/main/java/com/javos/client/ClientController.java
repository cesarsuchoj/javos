package com.javos.client;
import com.javos.client.dto.ClientRequest;
import com.javos.client.dto.ClientResponse;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes. Permite filtrar por nome ou documento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<ClientResponse>> findAll(
            @Parameter(description = "Filtrar por nome (busca parcial)") @RequestParam(required = false) String name,
            @Parameter(description = "Filtrar por documento (CPF/CNPJ)") @RequestParam(required = false) String document) {
        if (name != null) return ResponseEntity.ok(clientService.searchByName(name));
        if (document != null) return ResponseEntity.ok(clientService.searchByDocument(document));
        return ResponseEntity.ok(clientService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(clientService.findById(id)); }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Conflito de dados (ex: documento já cadastrado)")
    })
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) { return ResponseEntity.ok(clientService.update(id, request)); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Cliente possui registros associados")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) { clientService.delete(id); return ResponseEntity.noContent().build(); }
}
