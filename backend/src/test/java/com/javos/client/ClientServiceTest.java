/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.client;

import com.javos.client.dto.ClientRequest;
import com.javos.client.dto.ClientResponse;
import com.javos.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientRequest request;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@example.com")
                .phone("11999990000")
                .document("12345678901")
                .active(true)
                .build();

        request = new ClientRequest();
        request.setName("João Silva");
        request.setEmail("joao@example.com");
        request.setPhone("11999990000");
        request.setDocument("12345678901");
        request.setActive(true);
    }

    @Test
    void findAll_returnsAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<ClientResponse> result = clientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("João Silva");
        verify(clientRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponse> result = clientService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientResponse result = clientService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@example.com");
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_validRequest_savesAndReturnsClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponse result = clientService.create(request);

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.isActive()).isTrue();
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_existingClient_updatesAndReturnsClient() {
        Client updated = Client.builder()
                .id(1L)
                .name("João Atualizado")
                .email("joao@example.com")
                .active(true)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(updated);

        request.setName("João Atualizado");
        ClientResponse result = clientService.update(1L, request);

        assertThat(result.getName()).isEqualTo("João Atualizado");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_nonExistingClient_throwsResourceNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingClient_setsInactiveAndSaves() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        clientService.delete(1L);

        assertThat(client.isActive()).isFalse();
        verify(clientRepository).save(client);
    }

    @Test
    void delete_nonExistingClient_throwsResourceNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchByName_returnsMatchingClients() {
        when(clientRepository.findByNameContainingIgnoreCase("João"))
                .thenReturn(List.of(client));

        List<ClientResponse> result = clientService.searchByName("João");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    void searchByDocument_returnsMatchingClients() {
        when(clientRepository.findByDocumentContaining("12345"))
                .thenReturn(List.of(client));

        List<ClientResponse> result = clientService.searchByDocument("12345");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocument()).isEqualTo("12345678901");
    }
}
