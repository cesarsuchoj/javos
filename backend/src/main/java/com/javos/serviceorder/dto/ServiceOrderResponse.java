package com.javos.serviceorder.dto;

import com.javos.serviceorder.ServiceOrderPriority;
import com.javos.serviceorder.ServiceOrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ServiceOrderResponse {
    private Long id;
    private String orderNumber;
    private Long clientId;
    private String clientName;
    private Long technicianId;
    private String technicianName;
    private ServiceOrderStatus status;
    private ServiceOrderPriority priority;
    private String description;
    private String diagnosis;
    private String solution;
    private BigDecimal laborCost;
    private LocalDate estimatedCompletion;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
