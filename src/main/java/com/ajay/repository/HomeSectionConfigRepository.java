package com.ajay.repository;

import com.ajay.domains.HomeSectionKey;
import com.ajay.model.HomeSectionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeSectionConfigRepository extends JpaRepository<HomeSectionConfig, Long> {
    Optional<HomeSectionConfig> findBySectionKey(HomeSectionKey sectionKey);
}

