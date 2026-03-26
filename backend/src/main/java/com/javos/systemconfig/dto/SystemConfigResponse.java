package com.javos.systemconfig.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigResponse {
    private Long id;
    private String key;
    private String value;
    private String description;
    private LocalDateTime updatedAt;
}
