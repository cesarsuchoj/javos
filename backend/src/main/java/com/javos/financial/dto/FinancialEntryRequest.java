package com.javos.financial.dto;

import com.javos.financial.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancialEntryRequest {
    @NotBlank
    private String description;
    @NotNull
    private EntryType type;
    @NotNull
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean paid;
    private Long categoryId;
    private Long accountId;
    private Long referenceId;
    private String referenceType;
    private String notes;
}
