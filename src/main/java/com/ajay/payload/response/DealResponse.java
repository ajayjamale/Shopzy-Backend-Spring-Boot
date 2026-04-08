package com.ajay.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DealResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String image;
    private String discountLabel;
    private String redirectLink;
    private Integer discount;
    private Integer displayOrder;
    private LocalDate startDate;
    private LocalDate endDate;
}
