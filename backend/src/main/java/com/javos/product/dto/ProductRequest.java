package com.javos.product.dto;

import com.javos.product.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @Size(max = 50)
    private String code;
    @NotBlank @Size(max = 150)
    private String name;
    private String description;
    @NotNull
    private ProductType type;
    @NotNull
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stockQty = 0;
    @Size(max = 20)
    private String unit;
    private boolean active = true;
}
