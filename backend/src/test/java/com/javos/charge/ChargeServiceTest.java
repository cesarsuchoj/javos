/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.charge;

import com.javos.charge.dto.ChargeRequest;
import com.javos.charge.dto.ChargeResponse;
import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ChargeService chargeService;

    private Charge charge;
    private ChargeRequest request;

    @BeforeEach
    void setUp() {
        charge = Charge.builder()
                .id(1L)
                .amount(new BigDecimal("250.00"))
                .dueDate(LocalDate.of(2025, 12, 31))
                .status(ChargeStatus.PENDING)
                .method(ChargeMethod.PIX)
                .notes("Test charge")
                .build();

        request = new ChargeRequest();
        request.setAmount(new BigDecimal("250.00"));
        request.setDueDate(LocalDate.of(2025, 12, 31));
        request.setStatus(ChargeStatus.PENDING);
        request.setMethod(ChargeMethod.PIX);
        request.setNotes("Test charge");
    }

    @Test
    void findAll_returnsAllCharges() {
        when(chargeRepository.findAll()).thenReturn(List.of(charge));

        List<ChargeResponse> result = chargeService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).getStatus()).isEqualTo(ChargeStatus.PENDING);
        verify(chargeRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(chargeRepository.findAll()).thenReturn(List.of());

        List<ChargeResponse> result = chargeService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsCharge() {
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        ChargeResponse result = chargeService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
        assertThat(result.getMethod()).isEqualTo(ChargeMethod.PIX);
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(chargeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_withoutClientId_savesAndReturnsCharge() {
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        ChargeResponse result = chargeService.create(request);

        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
        assertThat(result.getStatus()).isEqualTo(ChargeStatus.PENDING);
        verify(chargeRepository).save(any(Charge.class));
        verifyNoInteractions(clientRepository);
    }

    @Test
    void create_withClientId_loadsClientAndSaves() {
        Client client = Client.builder().id(10L).name("Test Client").active(true).build();
        Charge chargeWithClient = Charge.builder()
                .id(2L)
                .client(client)
                .amount(new BigDecimal("100.00"))
                .status(ChargeStatus.PENDING)
                .build();

        request.setClientId(10L);
        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(chargeRepository.save(any(Charge.class))).thenReturn(chargeWithClient);

        ChargeResponse result = chargeService.create(request);

        assertThat(result.getClientId()).isEqualTo(10L);
        verify(clientRepository).findById(10L);
    }

    @Test
    void create_withInvalidClientId_throwsResourceNotFoundException() {
        request.setClientId(999L);
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(chargeRepository, never()).save(any());
    }

    @Test
    void create_withNullStatus_defaultsToPending() {
        request.setStatus(null);
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        chargeService.create(request);

        verify(chargeRepository).save(argThat(c -> c.getStatus() == ChargeStatus.PENDING));
    }

    @Test
    void update_existingCharge_updatesAndReturns() {
        Charge updated = Charge.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .status(ChargeStatus.PAID)
                .notes("Updated charge")
                .build();

        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenReturn(updated);

        request.setAmount(new BigDecimal("500.00"));
        ChargeResponse result = chargeService.update(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        verify(chargeRepository).save(any(Charge.class));
    }

    @Test
    void update_nonExistingCharge_throwsResourceNotFoundException() {
        when(chargeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withClientId_loadsClient() {
        Client client = Client.builder().id(10L).name("Client").active(true).build();
        request.setClientId(10L);

        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        chargeService.update(1L, request);

        verify(clientRepository).findById(10L);
    }

    @Test
    void updateStatus_existingCharge_updatesStatus() {
        Charge paid = Charge.builder().id(1L).amount(new BigDecimal("250.00")).status(ChargeStatus.PAID).build();
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenReturn(paid);

        ChargeResponse result = chargeService.updateStatus(1L, ChargeStatus.PAID);

        assertThat(result.getStatus()).isEqualTo(ChargeStatus.PAID);
        verify(chargeRepository).save(charge);
    }

    @Test
    void updateStatus_nonExistingCharge_throwsResourceNotFoundException() {
        when(chargeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.updateStatus(999L, ChargeStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingCharge_deletesCharge() {
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        chargeService.delete(1L);

        verify(chargeRepository).delete(charge);
    }

    @Test
    void delete_nonExistingCharge_throwsResourceNotFoundException() {
        when(chargeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_chargeWithClientAndName_responseIncludesClientName() {
        Client client = Client.builder().id(5L).name("Acme Corp").active(true).build();
        Charge chargeWithClient = Charge.builder()
                .id(3L)
                .client(client)
                .amount(new BigDecimal("750.00"))
                .status(ChargeStatus.PENDING)
                .build();

        request.setClientId(5L);
        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));
        when(chargeRepository.save(any(Charge.class))).thenReturn(chargeWithClient);

        ChargeResponse result = chargeService.create(request);

        assertThat(result.getClientName()).isEqualTo("Acme Corp");
    }
}
