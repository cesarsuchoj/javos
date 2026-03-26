package com.javos.charge.dto;

import com.javos.charge.ChargeMethod;
import com.javos.charge.ChargeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChargeRequest {
    private Long clientId;
    private Long referenceId;
    private String referenceType;
    @NotNull
    private BigDecimal amount;
    private LocalDate dueDate;
    private ChargeStatus status;
    private ChargeMethod method;
    private String externalId;
    private String notes;
}
