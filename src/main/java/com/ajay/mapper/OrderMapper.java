package com.ajay.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.ajay.domains.OrderStatus;
import com.ajay.payload.response.OrderHistory;
import com.ajay.payload.response.OrderItemResponse;
import com.ajay.payload.response.OrderResponse;
import com.ajay.model.Order;
import com.ajay.model.OrderItem;
import com.ajay.model.User;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderItemResponse toResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProduct(ProductMapper.toResponse(orderItem.getProduct()));
        response.setSize(orderItem.getSize());
        response.setQuantity(orderItem.getQuantity());
        response.setMrpPrice(orderItem.getMrpPrice());
        response.setSellingPrice(orderItem.getSellingPrice());
        return response;
    }

    public static OrderItem toEntity(OrderItemResponse response) {
        if (response == null) {
            return null;
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setId(response.getId());
        orderItem.setSize(response.getSize());
        orderItem.setQuantity(response.getQuantity());
        orderItem.setMrpPrice(response.getMrpPrice());
        orderItem.setSellingPrice(response.getSellingPrice());
        return orderItem;
    }

    public static OrderItem updateEntity(OrderItem orderItem, OrderItemResponse response) {
        if (orderItem == null || response == null) {
            return orderItem;
        }
        orderItem.setSize(response.getSize());
        orderItem.setQuantity(response.getQuantity());
        orderItem.setMrpPrice(response.getMrpPrice());
        orderItem.setSellingPrice(response.getSellingPrice());
        return orderItem;
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderId(order.getOrderId());
        response.setUser(UserMapper.toResponse(order.getUser()));
        response.setSellerId(order.getSellerId());
        response.setOrderItems(order.getOrderItems().stream().map(OrderMapper::toResponse).collect(Collectors.toList()));
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentDetails(order.getPaymentDetails());
        response.setTotalMrpPrice(order.getTotalMrpPrice());
        response.setTotalSellingPrice(order.getTotalSellingPrice());
        response.setDiscount(order.getDiscount());
        response.setOrderStatus(order.getOrderStatus());
        response.setTotalItem(order.getTotalItem());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderDate(order.getOrderDate());
        response.setDeliverDate(order.getDeliverDate());

        return response;
    }

    public static Order toEntity(OrderResponse response) {
        if (response == null) {
            return null;
        }

        Order order = new Order();
        order.setId(response.getId());
        order.setOrderId(response.getOrderId());
        order.setSellerId(response.getSellerId());
        order.setShippingAddress(response.getShippingAddress());
        order.setPaymentDetails(response.getPaymentDetails());
        order.setTotalMrpPrice(response.getTotalMrpPrice());
        order.setTotalSellingPrice(response.getTotalSellingPrice());
        order.setDiscount(response.getDiscount());
        order.setOrderStatus(response.getOrderStatus());
        order.setTotalItem(response.getTotalItem());
        order.setPaymentStatus(response.getPaymentStatus());
        order.setOrderDate(response.getOrderDate());
        order.setDeliverDate(response.getDeliverDate());

        return order;
    }

    public static Order updateEntity(Order order, OrderResponse response) {
        if (order == null || response == null) {
            return order;
        }
        order.setShippingAddress(response.getShippingAddress());
        order.setPaymentDetails(response.getPaymentDetails());
        order.setTotalMrpPrice(response.getTotalMrpPrice());
        order.setTotalSellingPrice(response.getTotalSellingPrice());
        order.setDiscount(response.getDiscount());
        order.setOrderStatus(response.getOrderStatus());
        order.setTotalItem(response.getTotalItem());
        order.setPaymentStatus(response.getPaymentStatus());
        order.setOrderDate(response.getOrderDate());
        order.setDeliverDate(response.getDeliverDate());
        return order;
    }

    public static OrderHistory toOrderHistory(List<Order> orders, User user) {
        if (orders == null || user == null) {
            return null;
        }

        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setUser(UserMapper.toResponse(user));

        List<OrderResponse> currentOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.CANCELLED)
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        orderHistory.setCurrentOrders(currentOrders);
        orderHistory.setTotalOrders(orders.size());

        int cancelledOrders = (int) orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CANCELLED)
                .count();
        orderHistory.setCancelledOrders(cancelledOrders);

        int completedOrders = (int) orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .count();
        orderHistory.setCompletedOrders(completedOrders);

        return orderHistory;
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        return toResponse(orderItem);
    }

    public static OrderItem toOrderItem(OrderItemResponse response) {
        return toEntity(response);
    }

    public static OrderResponse toOrderResponse(Order order) {
        return toResponse(order);
    }

    public static Order toOrder(OrderResponse response) {
        return toEntity(response);
    }
}
