package com.ajay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	 Cart findByUserId(Long userId);
}
