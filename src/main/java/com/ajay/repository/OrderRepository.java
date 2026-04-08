package com.ajay.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.domains.PaymentStatus;
import com.ajay.model.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByUserId(Long userId);
    List<Order> findBySellerIdOrderByOrderDateDesc(Long sellerId);
    List<Order> findByUserIdAndPaymentStatusOrderByOrderDateDesc(Long userId, PaymentStatus paymentStatus);
    List<Order> findBySellerIdAndPaymentStatusOrderByOrderDateDesc(Long sellerId, PaymentStatus paymentStatus);
    List<Order> findBySellerIdAndOrderDateBetween(Long sellerId,LocalDateTime startDate, LocalDateTime endDate);

}

