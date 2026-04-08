package com.ajay.service.impl;

import com.ajay.domains.HomeSectionKey;
import com.ajay.model.HomeContentItem;
import com.ajay.model.HomeSectionConfig;
import com.ajay.mapper.DailyDiscountMapper;
import com.ajay.mapper.DealMapper;
import com.ajay.mapper.HomeContentItemMapper;
import com.ajay.mapper.HomeSectionConfigMapper;
import com.ajay.repository.DailyDiscountRepository;
import com.ajay.repository.DealRepository;
import com.ajay.repository.HomeContentItemRepository;
import com.ajay.repository.HomeSectionConfigRepository;
import com.ajay.payload.request.HomeContentItemRequest;
import com.ajay.payload.response.DailyDiscountResponse;
import com.ajay.payload.response.HomeContentItemResponse;
import com.ajay.payload.response.HomePageResponse;
import com.ajay.payload.response.HomeSectionConfigResponse;
import com.ajay.payload.response.DealResponse;
import com.ajay.service.HomeContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeContentServiceImpl implements HomeContentService {

    private final HomeContentItemRepository itemRepository;
    private final HomeSectionConfigRepository sectionConfigRepository;
    private final DealRepository dealRepository;
    private final DailyDiscountRepository dailyDiscountRepository;

    @Override
    public HomeContentItemResponse createItem(HomeContentItemRequest request) {
        HomeContentItem contentItem = HomeContentItemMapper.toEntity(request);
        HomeContentItem saved = itemRepository.save(contentItem);
        return HomeContentItemMapper.toResponse(saved);
    }

    @Override
    public HomeContentItemResponse updateItem(Long id, HomeContentItemRequest request) {
        HomeContentItem existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        HomeContentItem updated = HomeContentItemMapper.updateEntity(existing, request);
        return HomeContentItemMapper.toResponse(itemRepository.save(updated));
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public HomeContentItemResponse updateStatus(Long id, boolean active) {
        HomeContentItem existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        existing.setActive(active);
        return HomeContentItemMapper.toResponse(itemRepository.save(existing));
    }

    @Override
    public HomeContentItemResponse updateDisplayOrder(Long id, Integer displayOrder) {
        HomeContentItem existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        existing.setDisplayOrder(displayOrder);
        return HomeContentItemMapper.toResponse(itemRepository.save(existing));
    }

    @Override
    public List<HomeContentItemResponse> getItems(HomeSectionKey sectionKey, boolean onlyActive) {
        List<HomeContentItem> items;
        if (sectionKey != null) {
            items = onlyActive
                    ? itemRepository.findBySectionKeyAndActiveTrueOrderByDisplayOrderAscIdAsc(sectionKey)
                    : itemRepository.findBySectionKeyOrderByDisplayOrderAscIdAsc(sectionKey);
        } else {
            items = itemRepository.findAll().stream()
                    .sorted(Comparator.comparing(HomeContentItem::getSectionKey).thenComparing(HomeContentItem::getDisplayOrder))
                    .toList();
            if (onlyActive) {
                items = items.stream().filter(HomeContentItem::isActive).toList();
            }
        }
        return items.stream().map(HomeContentItemMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<HomeSectionConfigResponse> getSectionConfigs() {
        ensureDefaultConfigs();
        return sectionConfigRepository.findAll().stream()
                .sorted(Comparator.comparing(HomeSectionConfig::getDisplayOrder))
                .map(HomeSectionConfigMapper::toResponse)
                .toList();
    }

    @Override
    public HomeSectionConfigResponse updateSectionConfig(HomeSectionKey sectionKey, HomeSectionConfigResponse request) {
        ensureDefaultConfigs();
        HomeSectionConfig config = sectionConfigRepository.findBySectionKey(sectionKey)
                .orElseGet(() -> defaultConfig(sectionKey));
        HomeSectionConfig updated = HomeSectionConfigMapper.updateEntity(config, request);
        return HomeSectionConfigMapper.toResponse(sectionConfigRepository.save(updated));
    }

    @Override
    public HomePageResponse getPublicHomePage() {
        ensureDefaultConfigs();
        List<HomeContentItemResponse> hero = getItems(HomeSectionKey.HERO, true);
        List<HomeContentItemResponse> electronics = getItems(HomeSectionKey.ELECTRONICS, true);
        List<HomeContentItemResponse> topBrand = getItems(HomeSectionKey.TOP_BRAND, true);
        List<HomeContentItemResponse> shop = getItems(HomeSectionKey.SHOP_BY_CATEGORY, true);
        var today = java.time.LocalDate.now();
        List<DealResponse> deals = dealRepository
                .findByActiveTrueAndFeaturedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(today, today)
                .stream().map(DealMapper::toResponse).toList();
        List<DailyDiscountResponse> dailyDiscounts = dailyDiscountRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(today, today)
                .stream().map(DailyDiscountMapper::toResponse).toList();
        List<HomeSectionConfigResponse> sections = getSectionConfigs().stream()
                .filter(HomeSectionConfigResponse::isVisible)
                .toList();
        return HomePageResponse.builder()
                .heroSlides(hero)
                .electronics(electronics)
                .topBrands(topBrand)
                .shopByCategories(shop)
                .deals(deals)
                .dailyDiscounts(dailyDiscounts)
                .sections(sections)
                .build();
    }

    @Override
    public HomeContentItem mapRequestToEntity(HomeContentItemRequest req, HomeContentItem existing) {
        return HomeContentItemMapper.updateEntity(existing, req);
    }

    private void ensureDefaultConfigs() {
        Arrays.stream(HomeSectionKey.values()).forEach(key -> {
            Optional<HomeSectionConfig> existing = sectionConfigRepository.findBySectionKey(key);
            if (existing.isEmpty()) {
                sectionConfigRepository.save(defaultConfig(key));
            }
        });
    }

    private HomeSectionConfig defaultConfig(HomeSectionKey key) {
        HomeSectionConfig sectionConfig = new HomeSectionConfig();
        sectionConfig.setSectionKey(key);
        sectionConfig.setSectionTitle(defaultTitle(key));
        sectionConfig.setVisible(true);
        sectionConfig.setDisplayOrder(switch (key) {
            case HERO -> 0;
            case ELECTRONICS -> 1;
            case TOP_BRAND -> 2;
            case SHOP_BY_CATEGORY -> 3;
        });
        return sectionConfig;
    }

    private String defaultTitle(HomeSectionKey key) {
        return switch (key) {
            case HERO -> "Featured";
            case ELECTRONICS -> "Electronics & Gadgets";
            case TOP_BRAND -> "Top Brands";
            case SHOP_BY_CATEGORY -> "Shop by category";
        };
    }

}
