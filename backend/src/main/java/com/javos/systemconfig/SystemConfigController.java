package com.javos.systemconfig;
import com.javos.systemconfig.dto.SystemConfigResponse;
import com.javos.systemconfig.dto.SystemConfigUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
public class SystemConfigController {
    private final SystemConfigService systemConfigService;
    @GetMapping public ResponseEntity<List<SystemConfigResponse>> findAll() { return ResponseEntity.ok(systemConfigService.findAll()); }
    @GetMapping("/{key}") public ResponseEntity<SystemConfigResponse> findByKey(@PathVariable String key) { return ResponseEntity.ok(systemConfigService.findByKey(key)); }
    @PutMapping("/{key}") public ResponseEntity<SystemConfigResponse> update(@PathVariable String key, @Valid @RequestBody SystemConfigUpdateRequest request) { return ResponseEntity.ok(systemConfigService.update(key, request)); }
}
