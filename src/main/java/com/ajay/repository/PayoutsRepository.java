package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.domain.PayoutsStatus;
import com.ajay.model.Payouts;

import java.util.List;

public interface PayoutsRepository extends JpaRepository<Payouts,Long> {

    List<Payouts> findPayoutsBySellerId(Long sellerId);
    List<Payouts> findAllByStatus(PayoutsStatus status);
}
