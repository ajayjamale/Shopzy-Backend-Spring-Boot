package com.ajay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ajay.domains.OrderStatus;
import com.ajay.exception.OrderException;
import com.ajay.exception.SellerException;
import com.ajay.model.Order;
import com.ajay.model.Seller;
import com.ajay.payload.response.ApiResponse;
import com.ajay.service.OrderService;
import com.ajay.service.SellerService;

import java.util.List;

@RestController
@RequestMapping({"/api/sellers/orders", "/seller/orders"})
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    private final SellerService sellerService;

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersHandler(
            @RequestHeader("Authorization") String jwt
    ) throws SellerException {
        Seller seller=sellerService.getSellerProfile(jwt);
        List<Order> orders=orderService.getShopsOrders(seller.getId());

        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{orderId}/status/{orderStatus}")
    public ResponseEntity<Order> updateOrderHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @PathVariable OrderStatus orderStatus
    ) throws OrderException, SellerException {
        Seller seller = sellerService.getSellerProfile(jwt);
        Order existingOrder = orderService.findOrderById(orderId);
        if (!seller.getId().equals(existingOrder.getSellerId())) {
            throw new OrderException("you can't update this order " + orderId);
        }

        Order updatedOrder = orderService.updateOrderStatus(orderId,orderStatus);

        return new ResponseEntity<>(updatedOrder,HttpStatus.ACCEPTED);
    }


    @DeleteMapping({"/{orderId}", "/{orderId}/delete"})
    public ResponseEntity<ApiResponse> deleteOrderHandler(@PathVariable Long orderId,
                                                          @RequestHeader("Authorization") String jwt) throws OrderException, SellerException {
        Seller seller = sellerService.getSellerProfile(jwt);
        Order existingOrder = orderService.findOrderById(orderId);
        if (!seller.getId().equals(existingOrder.getSellerId())) {
            throw new OrderException("you can't delete this order " + orderId);
        }
        orderService.deleteOrder(orderId);
        ApiResponse apiResponse = new ApiResponse("Order Deleted Successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.ACCEPTED);
    }

}

