package com.ajay.payload.response;

import com.ajay.domains.HomeSectionKey;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HomeContentItemResponse {
    private Long id;
    private HomeSectionKey sectionKey;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String buttonText;
    private String buttonLink;
    private String badgeText;
    private String redirectLink;
    private String categoryId;
    private boolean active;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

