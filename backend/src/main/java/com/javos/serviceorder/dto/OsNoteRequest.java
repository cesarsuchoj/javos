package com.javos.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OsNoteRequest {
    @NotBlank
    private String content;
    private Long authorId;
}
