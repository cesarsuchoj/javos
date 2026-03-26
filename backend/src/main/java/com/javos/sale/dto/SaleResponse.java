package com.javos.sale.dto;

import com.javos.sale.SaleStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private Long clientId;
    private String clientName;
    private Long sellerId;
    private String sellerName;
    private SaleStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private String notes;
    private LocalDate saleDate;
    private List<SaleItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
