package com.javos.product;
import com.javos.product.dto.ProductRequest;
import com.javos.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    @GetMapping public ResponseEntity<List<ProductResponse>> findAll(@RequestParam(required=false) String name) { if (name != null) return ResponseEntity.ok(productService.searchByName(name)); return ResponseEntity.ok(productService.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<ProductResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(productService.findById(id)); }
    @GetMapping("/code/{code}") public ResponseEntity<ProductResponse> findByCode(@PathVariable String code) { return ResponseEntity.ok(productService.findByCode(code)); }
    @PostMapping public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { return ResponseEntity.ok(productService.update(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { productService.delete(id); return ResponseEntity.noContent().build(); }
}
