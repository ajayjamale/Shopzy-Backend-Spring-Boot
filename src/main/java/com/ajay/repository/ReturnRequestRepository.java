package com.ajay.repository;

import com.ajay.domains.ReturnStatus;
import com.ajay.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReturnRequest> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Optional<ReturnRequest> findFirstByOrderItemIdAndUserIdAndStatusIn(
            Long orderItemId,
            Long userId,
            Collection<ReturnStatus> statuses
    );
}

