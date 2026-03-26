package com.javos.sale.dto;

import com.javos.sale.SaleStatus;
import jakarta.validation.constraints.NotNull;
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
    private BigDecimal discount;
    private String notes;
    private LocalDate saleDate;
    private List<SaleItemRequest> items;
}
