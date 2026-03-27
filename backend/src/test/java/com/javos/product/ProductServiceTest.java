/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.product;

import com.javos.exception.ResourceNotFoundException;
import com.javos.product.dto.ProductRequest;
import com.javos.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .code("PROD-001")
                .name("Teclado Mecânico")
                .type(ProductType.PRODUCT)
                .price(new BigDecimal("299.90"))
                .cost(new BigDecimal("150.00"))
                .stockQty(10)
                .active(true)
                .build();

        request = new ProductRequest();
        request.setCode("PROD-001");
        request.setName("Teclado Mecânico");
        request.setType(ProductType.PRODUCT);
        request.setPrice(new BigDecimal("299.90"));
        request.setCost(new BigDecimal("150.00"));
        request.setStockQty(10);
        request.setActive(true);
    }

    @Test
    void findAll_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Teclado Mecânico");
        verify(productRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponse> result = productService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Teclado Mecânico");
        assertThat(result.getCode()).isEqualTo("PROD-001");
        assertThat(result.getPrice()).isEqualByComparingTo("299.90");
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findByCode_existingCode_returnsProduct() {
        when(productRepository.findByCode("PROD-001")).thenReturn(Optional.of(product));

        ProductResponse result = productService.findByCode("PROD-001");

        assertThat(result.getCode()).isEqualTo("PROD-001");
    }

    @Test
    void findByCode_nonExistingCode_throwsResourceNotFoundException() {
        when(productRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findByCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void create_validRequest_savesAndReturnsProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Teclado Mecânico");
        assertThat(result.getType()).isEqualTo(ProductType.PRODUCT);
        assertThat(result.isActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_withNullStockQty_defaultsToZero() {
        request.setStockQty(null);
        Product productWithZeroStock = Product.builder()
                .id(2L)
                .name("Serviço de Manutenção")
                .type(ProductType.SERVICE)
                .price(new BigDecimal("100.00"))
                .stockQty(0)
                .active(true)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(productWithZeroStock);

        ProductResponse result = productService.create(request);

        assertThat(result.getStockQty()).isEqualTo(0);
    }

    @Test
    void update_existingProduct_updatesAndReturnsProduct() {
        Product updated = Product.builder()
                .id(1L)
                .code("PROD-001")
                .name("Teclado Mecânico RGB")
                .type(ProductType.PRODUCT)
                .price(new BigDecimal("349.90"))
                .stockQty(8)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        request.setName("Teclado Mecânico RGB");
        request.setPrice(new BigDecimal("349.90"));
        ProductResponse result = productService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Teclado Mecânico RGB");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_nonExistingProduct_throwsResourceNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingProduct_setsInactiveAndSaves() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.delete(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void delete_nonExistingProduct_throwsResourceNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchByName_returnsMatchingProducts() {
        when(productRepository.findByNameContainingIgnoreCase("Teclado"))
                .thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchByName("Teclado");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Teclado Mecânico");
    }
}
