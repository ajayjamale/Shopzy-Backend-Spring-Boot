package com.ajay.mapper;

import com.ajay.model.HomeSectionConfig;
import com.ajay.payload.response.HomeSectionConfigResponse;

public class HomeSectionConfigMapper {

    private HomeSectionConfigMapper() {
    }

    public static HomeSectionConfig toEntity(HomeSectionConfigResponse response) {
        if (response == null) {
            return null;
        }
        HomeSectionConfig sectionConfig = new HomeSectionConfig();
        sectionConfig.setSectionKey(response.getSectionKey());
        sectionConfig.setSectionTitle(response.getSectionTitle());
        sectionConfig.setVisible(response.isVisible());
        sectionConfig.setDisplayOrder(response.getDisplayOrder());
        return sectionConfig;
    }

    public static HomeSectionConfig updateEntity(HomeSectionConfig sectionConfig, HomeSectionConfigResponse response) {
        if (sectionConfig == null || response == null) {
            return sectionConfig;
        }
        if (response.getSectionKey() != null) {
            sectionConfig.setSectionKey(response.getSectionKey());
        }
        if (response.getSectionTitle() != null) {
            sectionConfig.setSectionTitle(response.getSectionTitle());
        }
        if (response.getDisplayOrder() != null) {
            sectionConfig.setDisplayOrder(response.getDisplayOrder());
        }
        sectionConfig.setVisible(response.isVisible());
        return sectionConfig;
    }

    public static HomeSectionConfigResponse toResponse(HomeSectionConfig sectionConfig) {
        if (sectionConfig == null) {
            return null;
        }
        return HomeSectionConfigResponse.builder()
                .sectionKey(sectionConfig.getSectionKey())
                .sectionTitle(sectionConfig.getSectionTitle())
                .visible(sectionConfig.isVisible())
                .displayOrder(sectionConfig.getDisplayOrder())
                .build();
    }
}
