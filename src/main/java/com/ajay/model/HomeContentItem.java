package com.ajay.model;

import com.ajay.domains.HomeSectionKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "home_content_items",
        indexes = {
                @Index(name = "idx_home_item_section", columnList = "sectionKey"),
                @Index(name = "idx_home_item_active", columnList = "active"),
                @Index(name = "idx_home_item_display_order", columnList = "displayOrder")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HomeSectionKey sectionKey;

    private String title;
    private String subtitle;
    @Column(length = 800)
    private String description;
    private String imageUrl;
    private String buttonText;
    private String buttonLink;
    private String badgeText;
    private String redirectLink;
    private String categoryId;

    private boolean active = true;
    private Integer displayOrder = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

