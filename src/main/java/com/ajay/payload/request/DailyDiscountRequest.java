package com.ajay.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDiscountRequest {
    @NotBlank
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String redirectLink;
    private String discountLabel;
    @PositiveOrZero
    @Max(100)
    private Integer discountPercent;
    private Boolean active;
    private Boolean highlighted;
    @PositiveOrZero
    private Integer displayOrder;
    private LocalDate startDate;
    private LocalDate endDate;
}
