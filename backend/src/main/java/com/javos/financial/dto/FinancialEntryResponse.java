package com.javos.financial.dto;

import com.javos.financial.EntryType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FinancialEntryResponse {
    private Long id;
    private String description;
    private EntryType type;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean paid;
    private Long categoryId;
    private String categoryName;
    private Long accountId;
    private String accountName;
    private Long referenceId;
    private String referenceType;
    private String notes;
    private LocalDateTime createdAt;
}
