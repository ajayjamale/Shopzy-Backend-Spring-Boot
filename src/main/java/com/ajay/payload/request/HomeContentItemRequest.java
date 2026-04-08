package com.ajay.payload.request;

import com.ajay.domains.HomeSectionKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class HomeContentItemRequest {
    @NotNull
    private HomeSectionKey sectionKey;

    @NotBlank
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String buttonText;
    private String buttonLink;
    private String badgeText;
    private String redirectLink;
    private String categoryId;
    private Boolean active;
    @PositiveOrZero
    private Integer displayOrder;
}

