package com.javos.serviceorder.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OsNoteResponse {
    private Long id;
    private Long serviceOrderId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}
