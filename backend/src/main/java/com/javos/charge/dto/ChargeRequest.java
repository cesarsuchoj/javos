package com.javos.charge.dto;

import com.javos.charge.ChargeMethod;
import com.javos.charge.ChargeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChargeRequest {
    private Long clientId;
    private Long referenceId;
    @Size(max = 50)
    private String referenceType;
    @NotNull
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;
    private LocalDate dueDate;
    private ChargeStatus status;
    private ChargeMethod method;
    @Size(max = 100)
    private String externalId;
    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
