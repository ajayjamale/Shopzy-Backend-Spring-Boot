package com.ajay.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ajay.domain.OrderStatus;
import com.ajay.domain.PaymentStatus;
import com.ajay.model.Order;
import com.ajay.model.User;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByUserIdAndPaymentStatusOrderByOrderDateDesc(Long userId, PaymentStatus paymentStatus);
    List<Order> findBySellerIdAndPaymentStatusOrderByOrderDateDesc(Long sellerId, PaymentStatus paymentStatus);
    List<Order> findBySellerIdAndOrderDateBetween(Long sellerId,LocalDateTime startDate, LocalDateTime endDate);

}
