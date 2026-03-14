package com.ajay.response;

import com.ajay.dto.OrderHistory;
import com.ajay.model.Cart;
import com.ajay.model.Order;
import com.ajay.model.Product;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse {
    private String functionName;
    private Cart userCart;
    private OrderHistory orderHistory;
    private Product product;
}
