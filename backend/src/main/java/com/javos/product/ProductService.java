package com.javos.product;

import com.javos.exception.ResourceNotFoundException;
import com.javos.product.dto.ProductRequest;
import com.javos.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse findById(Long id) {
        return toResponse(getProduct(id));
    }

    public ProductResponse findByCode(String code) {
        return toResponse(productRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + code)));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .price(request.getPrice())
                .cost(request.getCost())
                .stockQty(request.getStockQty() != null ? request.getStockQty() : 0)
                .unit(request.getUnit())
                .active(request.isActive())
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setType(request.getType());
        product.setPrice(request.getPrice());
        product.setCost(request.getCost());
        if (request.getStockQty() != null) product.setStockQty(request.getStockQty());
        product.setUnit(request.getUnit());
        product.setActive(request.isActive());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .type(product.getType())
                .price(product.getPrice())
                .cost(product.getCost())
                .stockQty(product.getStockQty())
                .unit(product.getUnit())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
