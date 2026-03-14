package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.HomeCategory;

public interface HomeCategoryRepository extends JpaRepository<HomeCategory,Long> {
}
