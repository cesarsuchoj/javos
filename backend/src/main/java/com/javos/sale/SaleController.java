package com.javos.sale;
import com.javos.sale.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;
    @GetMapping public ResponseEntity<List<SaleResponse>> findAll() { return ResponseEntity.ok(saleService.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<SaleResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(saleService.findById(id)); }
    @PostMapping public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(saleService.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<SaleResponse> update(@PathVariable Long id, @Valid @RequestBody SaleRequest request) { return ResponseEntity.ok(saleService.update(id, request)); }
    @PatchMapping("/{id}/status") public ResponseEntity<SaleResponse> changeStatus(@PathVariable Long id, @RequestParam SaleStatus status) { return ResponseEntity.ok(saleService.changeStatus(id, status)); }
    @PostMapping("/{id}/items") public ResponseEntity<SaleResponse> addItem(@PathVariable Long id, @Valid @RequestBody SaleItemRequest request) { return ResponseEntity.ok(saleService.addItem(id, request)); }
    @DeleteMapping("/{id}/items/{itemId}") public ResponseEntity<SaleResponse> removeItem(@PathVariable Long id, @PathVariable Long itemId) { return ResponseEntity.ok(saleService.removeItem(id, itemId)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { saleService.delete(id); return ResponseEntity.noContent().build(); }
}
