package com.ajay.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentFailureRequest {
    @NotNull
    @Positive
    private Long paymentOrderId;

    private String reason;
}
