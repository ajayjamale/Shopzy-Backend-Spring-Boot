package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Wishlist findByUserId(Long userId);
}
