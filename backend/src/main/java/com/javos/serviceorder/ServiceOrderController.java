package com.javos.serviceorder;
import com.javos.serviceorder.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;
    @GetMapping public ResponseEntity<List<ServiceOrderResponse>> findAll() { return ResponseEntity.ok(serviceOrderService.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<ServiceOrderResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(serviceOrderService.findById(id)); }
    @PostMapping public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<ServiceOrderResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceOrderRequest request) { return ResponseEntity.ok(serviceOrderService.update(id, request)); }
    @PatchMapping("/{id}/status") public ResponseEntity<ServiceOrderResponse> changeStatus(@PathVariable Long id, @RequestParam ServiceOrderStatus status) { return ResponseEntity.ok(serviceOrderService.changeStatus(id, status)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { serviceOrderService.delete(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/notes") public ResponseEntity<List<OsNoteResponse>> getNotes(@PathVariable Long id) { return ResponseEntity.ok(serviceOrderService.getNotes(id)); }
    @PostMapping("/{id}/notes") public ResponseEntity<OsNoteResponse> addNote(@PathVariable Long id, @Valid @RequestBody OsNoteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.addNote(id, request)); }
}
