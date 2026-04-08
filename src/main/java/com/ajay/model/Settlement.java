package com.ajay.model;

import com.ajay.domains.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlements",
        indexes = {
                @Index(name = "idx_settlement_seller", columnList = "sellerId"),
                @Index(name = "idx_settlement_order", columnList = "orderId"),
                @Index(name = "idx_settlement_order_item", columnList = "orderItemId"),
                @Index(name = "idx_settlement_status", columnList = "settlementStatus"),
                @Index(name = "idx_settlement_dates", columnList = "settlementDate,createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    private Long orderId;

    private Long orderItemId;

    private String transactionId;

    private String orderReference;

    private Long grossAmount;

    private Long commissionAmount;

    private Long platformFee;

    private Long taxAmount;

    private Long netSettlementAmount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    private String paymentMethod;

    private LocalDateTime settlementDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String remarks;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.settlementDate == null) {
            this.settlementDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

