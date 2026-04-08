package com.ajay.service;

import com.ajay.domains.HomeSectionKey;
import com.ajay.model.HomeContentItem;
import com.ajay.payload.request.HomeContentItemRequest;
import com.ajay.payload.response.HomeContentItemResponse;
import com.ajay.payload.response.HomePageResponse;
import com.ajay.payload.response.HomeSectionConfigResponse;

import java.util.List;

public interface HomeContentService {

    HomeContentItemResponse createItem(HomeContentItemRequest request);

    HomeContentItemResponse updateItem(Long id, HomeContentItemRequest request);

    void deleteItem(Long id);

    HomeContentItemResponse updateStatus(Long id, boolean active);

    HomeContentItemResponse updateDisplayOrder(Long id, Integer displayOrder);

    List<HomeContentItemResponse> getItems(HomeSectionKey sectionKey, boolean onlyActive);

    List<HomeSectionConfigResponse> getSectionConfigs();

    HomeSectionConfigResponse updateSectionConfig(HomeSectionKey sectionKey, HomeSectionConfigResponse request);

    HomePageResponse getPublicHomePage();

    HomeContentItem mapRequestToEntity(HomeContentItemRequest req, HomeContentItem existing);
}

