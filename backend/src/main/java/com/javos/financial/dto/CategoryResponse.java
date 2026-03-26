package com.javos.financial.dto;

import com.javos.financial.CategoryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String description;
    private boolean active;
}
