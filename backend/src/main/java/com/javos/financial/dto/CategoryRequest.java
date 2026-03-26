package com.javos.financial.dto;

import com.javos.financial.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @NotNull
    private CategoryType type;
    private String description;
    private boolean active = true;
}
