package com.javos.systemconfig.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemConfigUpdateRequest {
    @NotNull
    private String value;
}
