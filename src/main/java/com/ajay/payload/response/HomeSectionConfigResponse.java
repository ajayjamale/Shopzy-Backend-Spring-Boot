package com.ajay.payload.response;

import com.ajay.domains.HomeSectionKey;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HomeSectionConfigResponse {
    private HomeSectionKey sectionKey;
    private String sectionTitle;
    private boolean visible;
    private Integer displayOrder;
}

