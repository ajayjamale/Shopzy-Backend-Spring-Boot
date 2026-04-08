package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Deal;

import java.time.LocalDate;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal,Long> {
    List<Deal> findByActiveTrueAndFeaturedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(LocalDate start, LocalDate end);
    List<Deal> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(LocalDate start, LocalDate end);
    List<Deal> findByActiveTrueOrderByDisplayOrderAscIdAsc();
}

