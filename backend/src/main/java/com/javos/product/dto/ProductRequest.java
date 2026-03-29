package com.javos.product.dto;

import com.javos.product.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;
    @NotNull
    private ProductType type;
    @NotNull
    @DecimalMin(value = "0.00", message = "Preço não pode ser negativo")
    private BigDecimal price;
    @DecimalMin(value = "0.00", message = "Custo não pode ser negativo")
    private BigDecimal cost;
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private Integer stockQty = 0;
    @Size(max = 20)
    private String unit;
    private boolean active = true;
}
