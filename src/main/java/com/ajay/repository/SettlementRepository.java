package com.ajay.repository;

import com.ajay.domains.SettlementStatus;
import com.ajay.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByOrderId(Long orderId);
    Optional<Settlement> findByOrderItemId(Long orderItemId);

    List<Settlement> findBySellerId(Long sellerId);

    List<Settlement> findBySettlementStatus(SettlementStatus status);

    List<Settlement> findBySellerIdAndSettlementStatus(Long sellerId, SettlementStatus status);

    List<Settlement> findBySettlementDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("select s from Settlement s where " +
            "(:sellerId is null or s.sellerId = :sellerId) and " +
            "(:status is null or s.settlementStatus = :status) and " +
            "(:from is null or s.settlementDate >= :from) and " +
            "(:to is null or s.settlementDate <= :to)")
    List<Settlement> findAllFiltered(@Param("sellerId") Long sellerId,
                                     @Param("status") SettlementStatus status,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);
}

