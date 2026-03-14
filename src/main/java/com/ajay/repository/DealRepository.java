package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Deal;

public interface DealRepository extends JpaRepository<Deal,Long> {

}
