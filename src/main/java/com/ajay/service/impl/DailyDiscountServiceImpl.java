package com.ajay.service.impl;

import com.ajay.model.DailyDiscount;
import com.ajay.model.Deal;
import com.ajay.mapper.DailyDiscountMapper;
import com.ajay.repository.DailyDiscountRepository;
import com.ajay.repository.DealRepository;
import com.ajay.payload.request.DailyDiscountRequest;
import com.ajay.payload.response.DailyDiscountResponse;
import com.ajay.service.DailyDiscountService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyDiscountServiceImpl implements DailyDiscountService {

    private final DailyDiscountRepository repository;
    private final DealRepository dealRepository;

    @PostConstruct
    public void bootstrapFromLegacyDeals() {
        if (repository.count() > 0 || dealRepository.count() == 0) {
            return;
        }

        List<DailyDiscount> migrated = dealRepository.findAll().stream()
                .sorted((a, b) -> {
                    int order = Integer.compare(a.getDisplayOrder() == null ? 0 : a.getDisplayOrder(), b.getDisplayOrder() == null ? 0 : b.getDisplayOrder());
                    if (order != 0) return order;
                    return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
                })
                .map(this::fromLegacyDeal)
                .toList();

        repository.saveAll(migrated);
    }

    @Override
    public DailyDiscountResponse create(DailyDiscountRequest request) {
        DailyDiscount dailyDiscount = DailyDiscountMapper.toEntity(request);
        return DailyDiscountMapper.toResponse(repository.save(dailyDiscount));
    }

    @Override
    public DailyDiscountResponse update(Long id, DailyDiscountRequest request) {
        DailyDiscount dailyDiscount = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Daily discount not found"));
        DailyDiscount updated = DailyDiscountMapper.updateEntity(dailyDiscount, request);
        return DailyDiscountMapper.toResponse(repository.save(updated));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public DailyDiscountResponse updateStatus(Long id, boolean active) {
        DailyDiscount dailyDiscount = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Daily discount not found"));
        dailyDiscount.setActive(active);
        return DailyDiscountMapper.toResponse(repository.save(dailyDiscount));
    }

    @Override
    public List<DailyDiscountResponse> getAll(boolean onlyActive) {
        List<DailyDiscount> items;
        if (onlyActive) {
            LocalDate today = LocalDate.now();
            items = repository
                    .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(
                            today,
                            today
                    );
        } else {
            items = repository.findAllByOrderByDisplayOrderAscIdAsc();
        }
        return items.stream().map(DailyDiscountMapper::toResponse).toList();
    }

    private Integer clampDiscount(Integer value) {
        if (value == null) return null;
        if (value < 1) return 1;
        if (value > 95) return 95;
        return value;
    }

    private DailyDiscount fromLegacyDeal(Deal deal) {
        DailyDiscount item = new DailyDiscount();
        item.setTitle(deal.getTitle() == null || deal.getTitle().isBlank() ? "Daily Discount" : deal.getTitle());
        item.setSubtitle(deal.getSubtitle());
        item.setDescription(deal.getDescription());
        item.setImageUrl(deal.getImage());
        item.setRedirectLink(
                deal.getRedirectLink() != null && !deal.getRedirectLink().isBlank()
                        ? deal.getRedirectLink()
                        : "/products"
        );
        item.setDiscountLabel(deal.getDiscountLabel());
        item.setDiscountPercent(clampDiscount(deal.getDiscount() == null ? 10 : deal.getDiscount()));
        item.setActive(Boolean.TRUE.equals(deal.getActive()));
        item.setHighlighted(Boolean.TRUE.equals(deal.getFeatured()));
        item.setDisplayOrder(deal.getDisplayOrder() == null ? 0 : deal.getDisplayOrder());
        item.setStartDate(deal.getStartDate() != null ? deal.getStartDate() : LocalDate.now());
        item.setEndDate(deal.getEndDate() != null ? deal.getEndDate() : LocalDate.now().plusDays(1));
        if (item.getDiscountLabel() == null || item.getDiscountLabel().isBlank()) {
            item.setDiscountLabel(item.getDiscountPercent() + "% OFF");
        }
        return item;
    }
}
