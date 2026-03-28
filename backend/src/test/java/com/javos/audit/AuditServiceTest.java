/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.audit;

import com.javos.audit.dto.AuditLogResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .username("admin")
                .action("CREATE")
                .entityType("Client")
                .entityId(42L)
                .details("Created client João Silva")
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void logAction_savesAuditLog() {
        auditService.logAction("admin", "CREATE", "Client", 42L, "Created client", "127.0.0.1");

        verify(auditLogRepository).save(argThat(log ->
                "admin".equals(log.getUsername())
                        && "CREATE".equals(log.getAction())
                        && "Client".equals(log.getEntityType())
                        && Long.valueOf(42L).equals(log.getEntityId())
                        && "127.0.0.1".equals(log.getIpAddress())
        ));
    }

    @Test
    void logAction_withNullEntityId_savesAuditLog() {
        auditService.logAction("user1", "LOGIN", null, null, "User logged in", "192.168.1.1");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void findAll_returnsAllLogs() {
        when(auditLogRepository.findAll()).thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        assertThat(result.get(0).getAction()).isEqualTo("CREATE");
        assertThat(result.get(0).getEntityType()).isEqualTo("Client");
        assertThat(result.get(0).getEntityId()).isEqualTo(42L);
        verify(auditLogRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(auditLogRepository.findAll()).thenReturn(List.of());

        List<AuditLogResponse> result = auditService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_returnsLogsForUser() {
        when(auditLogRepository.findByUsernameOrderByCreatedAtDesc("admin"))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.findByUsername("admin");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        verify(auditLogRepository).findByUsernameOrderByCreatedAtDesc("admin");
    }

    @Test
    void findByUsername_noLogsForUser_returnsEmptyList() {
        when(auditLogRepository.findByUsernameOrderByCreatedAtDesc("unknownuser"))
                .thenReturn(List.of());

        List<AuditLogResponse> result = auditService.findByUsername("unknownuser");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEntity_returnsLogsForEntity() {
        when(auditLogRepository.findByEntityTypeAndEntityId("Client", 42L))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.findByEntity("Client", 42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityType()).isEqualTo("Client");
        assertThat(result.get(0).getEntityId()).isEqualTo(42L);
        verify(auditLogRepository).findByEntityTypeAndEntityId("Client", 42L);
    }

    @Test
    void findByEntity_noLogsForEntity_returnsEmptyList() {
        when(auditLogRepository.findByEntityTypeAndEntityId("Product", 999L))
                .thenReturn(List.of());

        List<AuditLogResponse> result = auditService.findByEntity("Product", 999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_responseFieldsMappedCorrectly() {
        when(auditLogRepository.findAll()).thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.findAll();

        AuditLogResponse resp = result.get(0);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(resp.getDetails()).isEqualTo("Created client João Silva");
        assertThat(resp.getCreatedAt()).isNotNull();
    }
}
