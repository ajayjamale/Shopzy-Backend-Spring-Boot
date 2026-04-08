package com.ajay.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    private String parentCategoryId;

    @Min(1)
    private int level;

    @NotBlank
    private String name;

    @NotBlank
    private String categoryId;
}
