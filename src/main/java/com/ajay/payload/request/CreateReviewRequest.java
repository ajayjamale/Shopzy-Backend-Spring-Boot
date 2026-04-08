package com.ajay.payload.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {
    @NotBlank
    @Size(min = 10, max = 2000)
    private String reviewText;

    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private double reviewRating;

    private List<String> productImages;
}
