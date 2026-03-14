package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
