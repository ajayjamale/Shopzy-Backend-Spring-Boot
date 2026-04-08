package com.ajay.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class OrderHistory {
    private Long id;
    private UserResponse user;
    private List<OrderResponse> currentOrders;
    private int totalOrders;
    private int cancelledOrders;
    private int completedOrders;
    private int pendingOrders;
}

