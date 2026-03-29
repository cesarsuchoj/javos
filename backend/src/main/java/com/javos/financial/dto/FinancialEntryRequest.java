package com.javos.financial.dto;

import com.javos.financial.EntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancialEntryRequest {
    @NotBlank
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String description;
    @NotNull
    private EntryType type;
    @NotNull
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean paid;
    private Long categoryId;
    private Long accountId;
    private Long referenceId;
    @Size(max = 50)
    private String referenceType;
    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
