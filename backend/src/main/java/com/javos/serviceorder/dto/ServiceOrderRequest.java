package com.javos.serviceorder.dto;

import com.javos.serviceorder.ServiceOrderPriority;
import com.javos.serviceorder.ServiceOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ServiceOrderRequest {
    @NotNull
    private Long clientId;
    private Long technicianId;
    private ServiceOrderStatus status;
    private ServiceOrderPriority priority;
    @NotBlank
    private String description;
    private String diagnosis;
    private String solution;
    private BigDecimal laborCost;
    private LocalDate estimatedCompletion;
}
