package com.javos.product.dto;

import com.javos.product.ProductType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private ProductType type;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stockQty;
    private String unit;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
