package com.ajay.payload.response;

import com.ajay.domains.SettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SettlementResponse {
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
    private SettlementStatus settlementStatus;
    private String paymentMethod;
    private LocalDateTime settlementDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String remarks;
}

