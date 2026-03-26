package com.javos.charge;
import com.javos.charge.dto.ChargeRequest;
import com.javos.charge.dto.ChargeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
public class ChargeController {
    private final ChargeService chargeService;
    @GetMapping public ResponseEntity<List<ChargeResponse>> findAll() { return ResponseEntity.ok(chargeService.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<ChargeResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(chargeService.findById(id)); }
    @PostMapping public ResponseEntity<ChargeResponse> create(@Valid @RequestBody ChargeRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(chargeService.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<ChargeResponse> update(@PathVariable Long id, @Valid @RequestBody ChargeRequest request) { return ResponseEntity.ok(chargeService.update(id, request)); }
    @PatchMapping("/{id}/status") public ResponseEntity<ChargeResponse> updateStatus(@PathVariable Long id, @RequestParam ChargeStatus status) { return ResponseEntity.ok(chargeService.updateStatus(id, status)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { chargeService.delete(id); return ResponseEntity.noContent().build(); }
}
