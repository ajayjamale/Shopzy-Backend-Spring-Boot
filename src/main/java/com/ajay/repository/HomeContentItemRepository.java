package com.ajay.repository;

import com.ajay.domains.HomeSectionKey;
import com.ajay.model.HomeContentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeContentItemRepository extends JpaRepository<HomeContentItem, Long> {
    List<HomeContentItem> findBySectionKeyOrderByDisplayOrderAscIdAsc(HomeSectionKey sectionKey);

    List<HomeContentItem> findBySectionKeyAndActiveTrueOrderByDisplayOrderAscIdAsc(HomeSectionKey sectionKey);
}

