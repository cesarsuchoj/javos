package com.javos.sale.dto;

import com.javos.sale.SaleStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {
    @NotNull
    private Long clientId;
    private Long sellerId;
    private SaleStatus status;
    @DecimalMin(value = "0.00", message = "Desconto não pode ser negativo")
    private BigDecimal discount;
    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
    private LocalDate saleDate;
    private List<SaleItemRequest> items;
}
