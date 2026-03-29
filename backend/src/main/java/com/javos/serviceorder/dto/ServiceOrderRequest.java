package com.javos.serviceorder.dto;

import com.javos.serviceorder.ServiceOrderPriority;
import com.javos.serviceorder.ServiceOrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String description;
    @Size(max = 2000, message = "Diagnóstico deve ter no máximo 2000 caracteres")
    private String diagnosis;
    @Size(max = 2000, message = "Solução deve ter no máximo 2000 caracteres")
    private String solution;
    @DecimalMin(value = "0.00", message = "Custo de mão de obra não pode ser negativo")
    private BigDecimal laborCost;
    private LocalDate estimatedCompletion;
}
