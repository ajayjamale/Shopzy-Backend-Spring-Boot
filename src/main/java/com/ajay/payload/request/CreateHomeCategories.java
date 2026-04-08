package com.ajay.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateHomeCategories {
    @NotBlank
    private String categoryId;

    @NotBlank
    private String name;

    private String image;
}
