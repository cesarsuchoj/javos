/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.systemconfig;

import com.javos.exception.ResourceNotFoundException;
import com.javos.systemconfig.dto.SystemConfigResponse;
import com.javos.systemconfig.dto.SystemConfigUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigService systemConfigService;

    private SystemConfig config;

    @BeforeEach
    void setUp() {
        config = SystemConfig.builder()
                .id(1L)
                .key("company.name")
                .value("Javos Ltda")
                .description("Nome da empresa")
                .build();
    }

    @Test
    void findAll_returnsAllConfigs() {
        when(systemConfigRepository.findAll()).thenReturn(List.of(config));

        List<SystemConfigResponse> result = systemConfigService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("company.name");
        assertThat(result.get(0).getValue()).isEqualTo("Javos Ltda");
        verify(systemConfigRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(systemConfigRepository.findAll()).thenReturn(List.of());

        List<SystemConfigResponse> result = systemConfigService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByKey_existingKey_returnsConfig() {
        when(systemConfigRepository.findByKey("company.name")).thenReturn(Optional.of(config));

        SystemConfigResponse result = systemConfigService.findByKey("company.name");

        assertThat(result.getKey()).isEqualTo("company.name");
        assertThat(result.getValue()).isEqualTo("Javos Ltda");
        assertThat(result.getDescription()).isEqualTo("Nome da empresa");
    }

    @Test
    void findByKey_nonExistingKey_throwsResourceNotFoundException() {
        when(systemConfigRepository.findByKey("unknown.key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemConfigService.findByKey("unknown.key"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown.key");
    }

    @Test
    void update_existingKey_updatesValueAndReturns() {
        SystemConfigUpdateRequest request = new SystemConfigUpdateRequest();
        request.setValue("Novo Nome");

        SystemConfig updated = SystemConfig.builder()
                .id(1L)
                .key("company.name")
                .value("Novo Nome")
                .description("Nome da empresa")
                .build();

        when(systemConfigRepository.findByKey("company.name")).thenReturn(Optional.of(config));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(updated);

        SystemConfigResponse result = systemConfigService.update("company.name", request);

        assertThat(result.getValue()).isEqualTo("Novo Nome");
        verify(systemConfigRepository).save(argThat(c -> "Novo Nome".equals(c.getValue())));
    }

    @Test
    void update_nonExistingKey_throwsResourceNotFoundException() {
        SystemConfigUpdateRequest request = new SystemConfigUpdateRequest();
        request.setValue("value");

        when(systemConfigRepository.findByKey("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemConfigService.update("nonexistent", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");

        verify(systemConfigRepository, never()).save(any());
    }

    @Test
    void findAll_responseFieldsMappedCorrectly() {
        when(systemConfigRepository.findAll()).thenReturn(List.of(config));

        List<SystemConfigResponse> result = systemConfigService.findAll();

        SystemConfigResponse resp = result.get(0);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getKey()).isEqualTo("company.name");
        assertThat(resp.getValue()).isEqualTo("Javos Ltda");
        assertThat(resp.getDescription()).isEqualTo("Nome da empresa");
    }
}
