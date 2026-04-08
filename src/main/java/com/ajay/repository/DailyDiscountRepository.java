package com.ajay.repository;

import com.ajay.model.DailyDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyDiscountRepository extends JpaRepository<DailyDiscount, Long> {
    List<DailyDiscount> findAllByOrderByDisplayOrderAscIdAsc();

    List<DailyDiscount> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(
            LocalDate startDate,
            LocalDate endDate
    );
}

