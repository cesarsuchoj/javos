package com.javos.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OsNoteRequest {
    @NotBlank
    @Size(max = 2000, message = "Conteúdo deve ter no máximo 2000 caracteres")
    private String content;
    private Long authorId;
}
