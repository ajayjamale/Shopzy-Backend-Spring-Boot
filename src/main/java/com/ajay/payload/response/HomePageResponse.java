package com.ajay.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomePageResponse {
    private List<HomeContentItemResponse> heroSlides;
    private List<HomeContentItemResponse> electronics;
    private List<HomeContentItemResponse> topBrands;
    private List<HomeContentItemResponse> shopByCategories;
    private List<DealResponse> deals;
    private List<DailyDiscountResponse> dailyDiscounts;
    private List<HomeSectionConfigResponse> sections;
}
