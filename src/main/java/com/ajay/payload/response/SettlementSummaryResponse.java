package com.ajay.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementSummaryResponse {
    private Long totalGrossAmount;
    private Long totalCommission;
    private Long totalPlatformFee;
    private Long totalTax;
    private Long totalNetAmount;
    private Long pendingCount;
    private Long processingCount;
    private Long eligibleCount;
    private Long onHoldCount;
    private Long completedCount;
    private Long failedCount;
    private Long cancelledCount;
}
