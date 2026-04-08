package com.ajay.payload.response;

import lombok.Data;

@Data
public class OrderItemResponse {

    private Long id;

    private ProductResponse product;

    private String size;

    private int quantity;

    private Integer mrpPrice;

    private Integer sellingPrice;

    private Long userId;

}

