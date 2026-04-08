package com.ajay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_discounts",
        indexes = {
                @Index(name = "idx_daily_discount_active", columnList = "active"),
                @Index(name = "idx_daily_discount_range", columnList = "startDate,endDate"),
                @Index(name = "idx_daily_discount_order", columnList = "displayOrder")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;

    @Column(length = 800)
    private String description;

    private String imageUrl;
    private String redirectLink;
    private String discountLabel;

    private Integer discountPercent;
    private boolean active = true;
    private boolean highlighted = false;
    private Integer displayOrder = 0;

    private LocalDate startDate;
    private LocalDate endDate;

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

