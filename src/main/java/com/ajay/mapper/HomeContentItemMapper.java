package com.ajay.mapper;

import com.ajay.model.HomeContentItem;
import com.ajay.payload.request.HomeContentItemRequest;
import com.ajay.payload.response.HomeContentItemResponse;

public class HomeContentItemMapper {



    public static HomeContentItem toEntity(HomeContentItemRequest request) {
        if (request == null) {
            return null;
        }
        return updateEntity(new HomeContentItem(), request);
    }

    public static HomeContentItem updateEntity(HomeContentItem homeContentItem, HomeContentItemRequest request) {
        if (homeContentItem == null || request == null) {
            return homeContentItem;
        }
        if (request.getSectionKey() != null) {
            homeContentItem.setSectionKey(request.getSectionKey());
        }
        if (request.getTitle() != null) {
            homeContentItem.setTitle(request.getTitle());
        }
        if (request.getSubtitle() != null) {
            homeContentItem.setSubtitle(request.getSubtitle());
        }
        if (request.getDescription() != null) {
            homeContentItem.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            homeContentItem.setImageUrl(request.getImageUrl());
        }
        if (request.getButtonText() != null) {
            homeContentItem.setButtonText(request.getButtonText());
        }
        if (request.getButtonLink() != null) {
            homeContentItem.setButtonLink(request.getButtonLink());
        }
        if (request.getBadgeText() != null) {
            homeContentItem.setBadgeText(request.getBadgeText());
        }
        if (request.getRedirectLink() != null) {
            homeContentItem.setRedirectLink(request.getRedirectLink());
        }
        if (request.getCategoryId() != null) {
            homeContentItem.setCategoryId(request.getCategoryId());
        }
        if (request.getActive() != null) {
            homeContentItem.setActive(request.getActive());
        }
        if (request.getDisplayOrder() != null) {
            homeContentItem.setDisplayOrder(request.getDisplayOrder());
        }
        return homeContentItem;
    }

    public static HomeContentItemResponse toResponse(HomeContentItem homeContentItem) {
        if (homeContentItem == null) {
            return null;
        }
        return HomeContentItemResponse.builder()
                .id(homeContentItem.getId())
                .sectionKey(homeContentItem.getSectionKey())
                .title(homeContentItem.getTitle())
                .subtitle(homeContentItem.getSubtitle())
                .description(homeContentItem.getDescription())
                .imageUrl(homeContentItem.getImageUrl())
                .buttonText(homeContentItem.getButtonText())
                .buttonLink(homeContentItem.getButtonLink())
                .badgeText(homeContentItem.getBadgeText())
                .redirectLink(homeContentItem.getRedirectLink())
                .categoryId(homeContentItem.getCategoryId())
                .active(homeContentItem.isActive())
                .displayOrder(homeContentItem.getDisplayOrder())
                .createdAt(homeContentItem.getCreatedAt())
                .updatedAt(homeContentItem.getUpdatedAt())
                .build();
    }
}
