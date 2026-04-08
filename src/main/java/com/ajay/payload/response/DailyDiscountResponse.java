package com.ajay.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyDiscountResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String redirectLink;
    private String discountLabel;
    private Integer discountPercent;
    private boolean active;
    private boolean highlighted;
    private Integer displayOrder;
    private LocalDate startDate;
    private LocalDate endDate;
}
