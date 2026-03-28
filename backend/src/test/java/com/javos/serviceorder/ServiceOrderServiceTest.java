/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.serviceorder;

import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import com.javos.model.User;
import com.javos.repository.UserRepository;
import com.javos.serviceorder.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private OsNoteRepository osNoteRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceOrderService serviceOrderService;

    private Client client;
    private User technician;
    private ServiceOrder serviceOrder;
    private ServiceOrderRequest request;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("Maria Santos")
                .active(true)
                .build();

        technician = User.builder()
                .id(2L)
                .username("tech01")
                .name("Carlos Técnico")
                .build();

        serviceOrder = ServiceOrder.builder()
                .id(1L)
                .orderNumber("OS20251201001")
                .client(client)
                .technician(technician)
                .status(ServiceOrderStatus.OPEN)
                .priority(ServiceOrderPriority.HIGH)
                .description("Notebook com tela quebrada")
                .laborCost(BigDecimal.ZERO)
                .build();

        request = new ServiceOrderRequest();
        request.setClientId(1L);
        request.setDescription("Notebook com tela quebrada");
        request.setPriority(ServiceOrderPriority.HIGH);
    }

    @Test
    void findAll_returnsAllServiceOrders() {
        when(serviceOrderRepository.findAll()).thenReturn(List.of(serviceOrder));

        List<ServiceOrderResponse> result = serviceOrderService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Notebook com tela quebrada");
        assertThat(result.get(0).getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        verify(serviceOrderRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(serviceOrderRepository.findAll()).thenReturn(List.of());

        List<ServiceOrderResponse> result = serviceOrderService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsServiceOrder() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));

        ServiceOrderResponse result = serviceOrderService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getClientName()).isEqualTo("Maria Santos");
        assertThat(result.getTechnicianId()).isEqualTo(2L);
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(serviceOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_withValidData_savesAndReturnsServiceOrder() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceOrderRepository.count()).thenReturn(0L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        ServiceOrderResponse result = serviceOrderService.create(request);

        assertThat(result.getDescription()).isEqualTo("Notebook com tela quebrada");
        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        verify(clientRepository).findById(1L);
        verify(serviceOrderRepository).save(any(ServiceOrder.class));
    }

    @Test
    void create_withNonExistingClient_throwsResourceNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void create_withTechnicianId_loadsTechnician() {
        request.setTechnicianId(2L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(serviceOrderRepository.count()).thenReturn(0L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.create(request);

        verify(userRepository).findById(2L);
        verify(serviceOrderRepository).save(argThat(o -> o.getTechnician() != null));
    }

    @Test
    void create_withInvalidTechnicianId_throwsResourceNotFoundException() {
        request.setTechnicianId(999L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withNullStatus_defaultsToOpen() {
        request.setStatus(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceOrderRepository.count()).thenReturn(0L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.create(request);

        verify(serviceOrderRepository).save(argThat(o -> o.getStatus() == ServiceOrderStatus.OPEN));
    }

    @Test
    void create_withNullPriority_defaultsToNormal() {
        request.setPriority(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceOrderRepository.count()).thenReturn(0L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.create(request);

        verify(serviceOrderRepository).save(argThat(o -> o.getPriority() == ServiceOrderPriority.NORMAL));
    }

    @Test
    void update_existingOrder_updatesAndReturns() {
        ServiceOrderRequest updateRequest = new ServiceOrderRequest();
        updateRequest.setClientId(1L);
        updateRequest.setDescription("Descrição atualizada");
        updateRequest.setPriority(ServiceOrderPriority.URGENT);

        ServiceOrder updated = ServiceOrder.builder()
                .id(1L)
                .orderNumber("OS20251201001")
                .client(client)
                .status(ServiceOrderStatus.OPEN)
                .priority(ServiceOrderPriority.URGENT)
                .description("Descrição atualizada")
                .laborCost(BigDecimal.ZERO)
                .build();

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(updated);

        ServiceOrderResponse result = serviceOrderService.update(1L, updateRequest);

        assertThat(result.getDescription()).isEqualTo("Descrição atualizada");
        assertThat(result.getPriority()).isEqualTo(ServiceOrderPriority.URGENT);
    }

    @Test
    void update_nonExistingOrder_throwsResourceNotFoundException() {
        when(serviceOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeStatus_toInProgress_updatesStatus() {
        ServiceOrder inProgress = ServiceOrder.builder()
                .id(1L)
                .client(client)
                .orderNumber("OS001")
                .status(ServiceOrderStatus.IN_PROGRESS)
                .priority(ServiceOrderPriority.HIGH)
                .description("Test")
                .laborCost(BigDecimal.ZERO)
                .build();

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(inProgress);

        ServiceOrderResponse result = serviceOrderService.changeStatus(1L, ServiceOrderStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
    }

    @Test
    void changeStatus_toDone_setsCompletedAt() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.changeStatus(1L, ServiceOrderStatus.DONE);

        verify(serviceOrderRepository).save(argThat(o -> o.getCompletedAt() != null));
    }

    @Test
    void changeStatus_toClosed_setsCompletedAt() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.changeStatus(1L, ServiceOrderStatus.CLOSED);

        verify(serviceOrderRepository).save(argThat(o -> o.getCompletedAt() != null));
    }

    @Test
    void changeStatus_nonExistingOrder_throwsResourceNotFoundException() {
        when(serviceOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.changeStatus(999L, ServiceOrderStatus.IN_PROGRESS))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingOrder_setsCancelledStatus() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(serviceOrder);

        serviceOrderService.delete(1L);

        assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        verify(serviceOrderRepository).save(serviceOrder);
    }

    @Test
    void delete_nonExistingOrder_throwsResourceNotFoundException() {
        when(serviceOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOrderService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addNote_validNote_savesAndReturnsNote() {
        OsNoteRequest noteRequest = new OsNoteRequest();
        noteRequest.setContent("Cliente ligou para verificar status");

        OsNote savedNote = OsNote.builder()
                .id(1L)
                .serviceOrder(serviceOrder)
                .content("Cliente ligou para verificar status")
                .createdAt(LocalDateTime.now())
                .build();

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(osNoteRepository.save(any(OsNote.class))).thenReturn(savedNote);

        OsNoteResponse result = serviceOrderService.addNote(1L, noteRequest);

        assertThat(result.getContent()).isEqualTo("Cliente ligou para verificar status");
        assertThat(result.getServiceOrderId()).isEqualTo(1L);
        verify(osNoteRepository).save(any(OsNote.class));
    }

    @Test
    void addNote_withAuthorId_loadsAuthor() {
        OsNoteRequest noteRequest = new OsNoteRequest();
        noteRequest.setContent("Nota com autor");
        noteRequest.setAuthorId(2L);

        OsNote savedNote = OsNote.builder()
                .id(2L)
                .serviceOrder(serviceOrder)
                .author(technician)
                .content("Nota com autor")
                .createdAt(LocalDateTime.now())
                .build();

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(osNoteRepository.save(any(OsNote.class))).thenReturn(savedNote);

        OsNoteResponse result = serviceOrderService.addNote(1L, noteRequest);

        assertThat(result.getAuthorId()).isEqualTo(2L);
        assertThat(result.getAuthorName()).isEqualTo("Carlos Técnico");
        verify(userRepository).findById(2L);
    }

    @Test
    void addNote_nonExistingServiceOrder_throwsResourceNotFoundException() {
        when(serviceOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        OsNoteRequest noteRequest = new OsNoteRequest();
        noteRequest.setContent("Nota");

        assertThatThrownBy(() -> serviceOrderService.addNote(999L, noteRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getNotes_existingOrder_returnsNotesSortedByCreatedAt() {
        OsNote note1 = OsNote.builder()
                .id(1L)
                .serviceOrder(serviceOrder)
                .content("Primeira nota")
                .createdAt(LocalDateTime.now())
                .build();

        when(osNoteRepository.findByServiceOrderIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(note1));

        List<OsNoteResponse> result = serviceOrderService.getNotes(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Primeira nota");
        verify(osNoteRepository).findByServiceOrderIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void findById_responseIncludesClientAndTechnicianNames() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));

        ServiceOrderResponse result = serviceOrderService.findById(1L);

        assertThat(result.getClientName()).isEqualTo("Maria Santos");
        assertThat(result.getTechnicianName()).isEqualTo("Carlos Técnico");
    }
}
