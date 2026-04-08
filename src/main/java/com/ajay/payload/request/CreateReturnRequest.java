package com.ajay.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateReturnRequest {
    @NotNull
    @Positive
    private Long orderId;

    @NotNull
    @Positive
    private Long orderItemId;

    @NotNull
    @Positive
    private Integer quantity;

    @NotBlank
    private String reason;
    private String description;
    private List<String> images = new ArrayList<>();
}
