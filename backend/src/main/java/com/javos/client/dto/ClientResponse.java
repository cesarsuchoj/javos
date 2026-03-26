package com.javos.client.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String document;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
