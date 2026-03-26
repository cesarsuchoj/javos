package com.javos.audit;
import com.javos.audit.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;
    @GetMapping public ResponseEntity<List<AuditLogResponse>> findAll() { return ResponseEntity.ok(auditService.findAll()); }
    @GetMapping("/user/{username}") public ResponseEntity<List<AuditLogResponse>> findByUsername(@PathVariable String username) { return ResponseEntity.ok(auditService.findByUsername(username)); }
    @GetMapping("/entity/{entityType}/{entityId}") public ResponseEntity<List<AuditLogResponse>> findByEntity(@PathVariable String entityType, @PathVariable Long entityId) { return ResponseEntity.ok(auditService.findByEntity(entityType, entityId)); }
}
