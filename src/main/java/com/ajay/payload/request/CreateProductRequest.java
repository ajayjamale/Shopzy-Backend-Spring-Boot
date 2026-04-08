package com.ajay.payload.request;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Positive
    private int mrpPrice;

    @Positive
    private int sellingPrice;

    // FIX 1: Added discountPercent — was missing entirely.
    // Without this field, Jackson silently drops the value sent from the
    // frontend, so the service always received 0 and that's what got saved.
    // The service will use this if > 0, or compute it as a fallback.
    @Min(0)
    private int discountPercent;

    // FIX 2: Added quantity — was missing entirely.
    // Products were always saved with quantity = 0 (int default),
    // making them appear out of stock or with no inventory on the customer site.
    @Min(0)
    private int quantity;

    @NotBlank
    private String brand;

    @NotBlank
    private String color;

    @NotEmpty
    private List<String> images;

    // These three are sent as category IDs (strings) from the frontend.
    // The service maps them to Category entities via CategoryRepository.
    @NotBlank
    private String category;

    @NotBlank
    private String category2;

    @NotBlank
    private String category3;

    @NotBlank
    private String sizes;
}
