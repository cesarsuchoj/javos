package com.javos.product;
import com.javos.product.dto.ProductRequest;
import com.javos.product.dto.ProductResponse;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos e serviços do catálogo")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna todos os produtos. Permite filtrar por nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<ProductResponse>> findAll(
            @Parameter(description = "Filtrar por nome (busca parcial)") @RequestParam(required = false) String name) {
        if (name != null) return ResponseEntity.ok(productService.searchByName(name));
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(productService.findById(id)); }

    @GetMapping("/code/{code}")
    @Operation(summary = "Buscar produto por código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductResponse> findByCode(@PathVariable String code) { return ResponseEntity.ok(productService.findByCode(code)); }

    @PostMapping
    @Operation(summary = "Criar produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Código de produto já cadastrado")
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { return ResponseEntity.ok(productService.update(id, request)); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto excluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) { productService.delete(id); return ResponseEntity.noContent().build(); }
}
