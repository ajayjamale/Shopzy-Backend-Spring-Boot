package com.ajay.payload.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ajay.domains.OrderStatus;
import com.ajay.domains.PaymentStatus;
import com.ajay.model.Address;
import com.ajay.model.PaymentDetails;

@Data
public class OrderResponse {

    private Long id;

    private String orderId;

    private UserResponse user;

    private Long sellerId;

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    private Address shippingAddress;

    private PaymentDetails paymentDetails=new PaymentDetails();

    private double totalMrpPrice;

    private Integer totalSellingPrice;

    private Integer discount;

    private OrderStatus orderStatus;

    private int totalItem;

    private PaymentStatus paymentStatus=PaymentStatus.PENDING;

    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime deliverDate = orderDate.plusDays(7);

}

