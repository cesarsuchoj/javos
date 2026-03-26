package com.javos.audit;

import com.javos.audit.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String username, String action, String entityType, Long entityId, String details, String ipAddress) {
        auditLogRepository.save(AuditLog.builder().username(username).action(action).entityType(entityType).entityId(entityId).details(details).ipAddress(ipAddress).build());
    }

    public List<AuditLogResponse> findAll() { return auditLogRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList()); }
    public List<AuditLogResponse> findByUsername(String username) { return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username).stream().map(this::toResponse).collect(Collectors.toList()); }
    public List<AuditLogResponse> findByEntity(String entityType, Long entityId) { return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId).stream().map(this::toResponse).collect(Collectors.toList()); }

    private AuditLogResponse toResponse(AuditLog log) { return AuditLogResponse.builder().id(log.getId()).username(log.getUsername()).action(log.getAction()).entityType(log.getEntityType()).entityId(log.getEntityId()).details(log.getDetails()).ipAddress(log.getIpAddress()).createdAt(log.getCreatedAt()).build(); }
}
