package com.ajay.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    private String title;
    private String description;
    private int mrpPrice;
    private int sellingPrice;

    // FIX 1: Added discountPercent — was missing entirely.
    // Without this field, Jackson silently drops the value sent from the
    // frontend, so the service always received 0 and that's what got saved.
    // The service will use this if > 0, or compute it as a fallback.
    private int discountPercent;

    // FIX 2: Added quantity — was missing entirely.
    // Products were always saved with quantity = 0 (int default),
    // making them appear out of stock or with no inventory on the customer site.
    private int quantity;

    private String brand;
    private String color;
    private List<String> images;

    // These three are sent as category IDs (strings) from the frontend.
    // The service maps them to Category entities via CategoryRepository.
    private String category;
    private String category2;
    private String category3;

    private String sizes;
}