package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.domain.AccountStatus;
import com.ajay.model.Seller;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller,Long> {

    Seller findByEmail(String email);
    List<Seller> findByAccountStatus(AccountStatus status);
}
