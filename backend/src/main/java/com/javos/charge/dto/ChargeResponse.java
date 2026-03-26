package com.javos.charge.dto;

import com.javos.charge.ChargeMethod;
import com.javos.charge.ChargeStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ChargeResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long referenceId;
    private String referenceType;
    private BigDecimal amount;
    private LocalDate dueDate;
    private ChargeStatus status;
    private ChargeMethod method;
    private String externalId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
