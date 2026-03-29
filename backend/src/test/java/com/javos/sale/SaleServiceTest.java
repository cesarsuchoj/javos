/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.sale;

import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import com.javos.model.User;
import com.javos.product.Product;
import com.javos.product.ProductRepository;
import com.javos.repository.UserRepository;
import com.javos.sale.dto.*;
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
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SaleService saleService;

    private Client client;
    private User seller;
    private Product product;
    private Sale sale;
    private SaleRequest request;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("Empresa ABC")
                .active(true)
                .build();

        seller = User.builder()
                .id(2L)
                .username("seller01")
                .name("João Vendedor")
                .build();

        product = Product.builder()
                .id(3L)
                .name("Notebook Dell")
                .price(new BigDecimal("3500.00"))
                .active(true)
                .build();

        sale = Sale.builder()
                .id(1L)
                .saleNumber("VD20251201001")
                .client(client)
                .seller(seller)
                .status(SaleStatus.OPEN)
                .totalAmount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .build();

        request = new SaleRequest();
        request.setClientId(1L);
        request.setSellerId(2L);
        request.setStatus(SaleStatus.OPEN);
    }

    @Test
    void findAll_returnsAllSales() {
        when(saleRepository.findAll()).thenReturn(List.of(sale));

        List<SaleResponse> result = saleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSaleNumber()).isEqualTo("VD20251201001");
        assertThat(result.get(0).getStatus()).isEqualTo(SaleStatus.OPEN);
        verify(saleRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(saleRepository.findAll()).thenReturn(List.of());

        List<SaleResponse> result = saleService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsSale() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        SaleResponse result = saleService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getClientName()).isEqualTo("Empresa ABC");
        assertThat(result.getSellerId()).isEqualTo(2L);
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_withValidData_savesAndReturnsSale() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleResponse result = saleService.create(request);

        assertThat(result.getSaleNumber()).isEqualTo("VD20251201001");
        assertThat(result.getStatus()).isEqualTo(SaleStatus.OPEN);
        verify(clientRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void create_withNonExistingClient_throwsResourceNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(saleRepository, never()).save(any());
    }

    @Test
    void create_withNullSellerId_doesNotLoadSeller() {
        request.setSellerId(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.create(request);

        verifyNoInteractions(userRepository);
    }

    @Test
    void create_withNullStatus_defaultsToOpen() {
        request.setStatus(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.create(request);

        verify(saleRepository).save(argThat(s -> s.getStatus() == SaleStatus.OPEN));
    }

    @Test
    void create_withNullDiscount_defaultsToZero() {
        request.setDiscount(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.create(request);

        verify(saleRepository).save(argThat(s -> s.getDiscount().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void create_withItems_addsItemsAndRecalculatesTotal() {
        SaleItemRequest itemRequest = new SaleItemRequest();
        itemRequest.setProductId(3L);
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(new BigDecimal("3500.00"));
        request.setItems(List.of(itemRequest));

        SaleItem saleItem = SaleItem.builder()
                .id(1L)
                .sale(sale)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("3500.00"))
                .discount(BigDecimal.ZERO)
                .totalPrice(new BigDecimal("7000.00"))
                .build();

        Sale savedSale = Sale.builder()
                .id(1L)
                .saleNumber("VD20251201001")
                .client(client)
                .status(SaleStatus.OPEN)
                .totalAmount(new BigDecimal("7000.00"))
                .discount(BigDecimal.ZERO)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale).thenReturn(savedSale);
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of(saleItem));

        SaleResponse result = saleService.create(request);

        verify(productRepository).findById(3L);
        verify(saleItemRepository).save(any(SaleItem.class));
    }

    @Test
    void create_withInvalidProductId_throwsResourceNotFoundException() {
        SaleItemRequest itemRequest = new SaleItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(1);
        itemRequest.setUnitPrice(new BigDecimal("100.00"));
        request.setItems(List.of(itemRequest));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_existingItem_updatesAndReturns() {
        SaleRequest updateRequest = new SaleRequest();
        updateRequest.setClientId(1L);
        updateRequest.setStatus(SaleStatus.CONFIRMED);
        updateRequest.setNotes("Updated notes");

        Sale updated = Sale.builder()
                .id(1L)
                .saleNumber("VD20251201001")
                .client(client)
                .status(SaleStatus.CONFIRMED)
                .totalAmount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .notes("Updated notes")
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(saleRepository.save(any(Sale.class))).thenReturn(updated);

        SaleResponse result = saleService.update(1L, updateRequest);

        assertThat(result.getStatus()).isEqualTo(SaleStatus.CONFIRMED);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void update_nonExistingSale_throwsResourceNotFoundException() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeStatus_existingSale_updatesStatus() {
        Sale confirmed = Sale.builder()
                .id(1L)
                .saleNumber("VD20251201001")
                .client(client)
                .status(SaleStatus.CONFIRMED)
                .totalAmount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(any(Sale.class))).thenReturn(confirmed);

        SaleResponse result = saleService.changeStatus(1L, SaleStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(SaleStatus.CONFIRMED);
    }

    @Test
    void changeStatus_nonExistingSale_throwsResourceNotFoundException() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.changeStatus(999L, SaleStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addItem_existingSale_addsItemAndUpdatesTotal() {
        SaleItemRequest itemRequest = new SaleItemRequest();
        itemRequest.setProductId(3L);
        itemRequest.setQuantity(1);
        itemRequest.setUnitPrice(new BigDecimal("3500.00"));

        SaleItem saleItem = SaleItem.builder()
                .id(1L)
                .sale(sale)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("3500.00"))
                .discount(BigDecimal.ZERO)
                .totalPrice(new BigDecimal("3500.00"))
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of(saleItem));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.addItem(1L, itemRequest);

        verify(productRepository).findById(3L);
        verify(saleItemRepository).save(any(SaleItem.class));
    }

    @Test
    void removeItem_existingSale_removesItemAndUpdatesTotal() {
        SaleItem saleItem = SaleItem.builder()
                .id(5L)
                .sale(sale)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("3500.00"))
                .discount(BigDecimal.ZERO)
                .totalPrice(new BigDecimal("3500.00"))
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of());
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.removeItem(1L, 5L);

        verify(saleItemRepository).deleteById(5L);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void delete_existingSale_setsCancelledStatus() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleService.delete(1L);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        verify(saleRepository).save(sale);
    }

    @Test
    void delete_nonExistingSale_throwsResourceNotFoundException() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withInvalidSellerId_throwsResourceNotFoundException() {
        request.setSellerId(999L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
