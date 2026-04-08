package com.ajay.payload.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SettlementRequest {
    @Positive
    private Long sellerId;
    @Positive
    private Long orderId;
    @Positive
    private Long orderItemId;
    private String transactionId;
    private String orderReference;

    @Positive
    private Long grossAmount;
    @PositiveOrZero
    private Long commissionAmount;
    @PositiveOrZero
    private Long platformFee;
    @PositiveOrZero
    private Long taxAmount;
    @PositiveOrZero
    private Long netSettlementAmount;

    private String paymentMethod;
    private String settlementDate; // ISO string, parsed in service
    private String remarks;
}
