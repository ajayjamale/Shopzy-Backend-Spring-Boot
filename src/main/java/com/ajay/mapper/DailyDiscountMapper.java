package com.ajay.mapper;

import java.time.LocalDate;

import com.ajay.model.DailyDiscount;
import com.ajay.payload.request.DailyDiscountRequest;
import com.ajay.payload.response.DailyDiscountResponse;

public class DailyDiscountMapper {

    private DailyDiscountMapper() {
    }

    public static DailyDiscount toEntity(DailyDiscountRequest request) {
        if (request == null) {
            return null;
        }
        return updateEntity(new DailyDiscount(), request);
    }

    public static DailyDiscount updateEntity(DailyDiscount dailyDiscount, DailyDiscountRequest request) {
        if (dailyDiscount == null || request == null) {
            return dailyDiscount;
        }
        if (request.getTitle() != null) {
            dailyDiscount.setTitle(request.getTitle().trim());
        }
        if (request.getSubtitle() != null) {
            dailyDiscount.setSubtitle(request.getSubtitle().trim());
        }
        if (request.getDescription() != null) {
            dailyDiscount.setDescription(request.getDescription().trim());
        }
        if (request.getImageUrl() != null) {
            dailyDiscount.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getRedirectLink() != null) {
            dailyDiscount.setRedirectLink(request.getRedirectLink().trim());
        }
        if (request.getDiscountLabel() != null) {
            dailyDiscount.setDiscountLabel(request.getDiscountLabel().trim());
        }
        if (request.getDiscountPercent() != null) {
            dailyDiscount.setDiscountPercent(clampDiscount(request.getDiscountPercent()));
        }
        if (request.getActive() != null) {
            dailyDiscount.setActive(request.getActive());
        }
        if (request.getHighlighted() != null) {
            dailyDiscount.setHighlighted(request.getHighlighted());
        }
        if (request.getDisplayOrder() != null) {
            dailyDiscount.setDisplayOrder(Math.max(0, request.getDisplayOrder()));
        }
        if (request.getStartDate() != null) {
            dailyDiscount.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            dailyDiscount.setEndDate(request.getEndDate());
        }

        if (dailyDiscount.getTitle() == null || dailyDiscount.getTitle().isBlank()) {
            dailyDiscount.setTitle("Daily Discount");
        }
        if (dailyDiscount.getImageUrl() == null || dailyDiscount.getImageUrl().isBlank()) {
            dailyDiscount.setImageUrl("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1000&q=80&fit=crop");
        }
        if (dailyDiscount.getRedirectLink() == null || dailyDiscount.getRedirectLink().isBlank()) {
            dailyDiscount.setRedirectLink("/products");
        }
        if (dailyDiscount.getDiscountPercent() == null) {
            dailyDiscount.setDiscountPercent(10);
        }
        if (dailyDiscount.getDiscountLabel() == null || dailyDiscount.getDiscountLabel().isBlank()) {
            dailyDiscount.setDiscountLabel(dailyDiscount.getDiscountPercent() + "% OFF");
        }

        LocalDate start = dailyDiscount.getStartDate() != null ? dailyDiscount.getStartDate() : LocalDate.now();
        LocalDate end = dailyDiscount.getEndDate() != null ? dailyDiscount.getEndDate() : start.plusDays(1);
        if (end.isBefore(start)) {
            end = start;
        }
        dailyDiscount.setStartDate(start);
        dailyDiscount.setEndDate(end);
        return dailyDiscount;
    }

    public static DailyDiscountResponse toResponse(DailyDiscount dailyDiscount) {
        if (dailyDiscount == null) {
            return null;
        }
        return DailyDiscountResponse.builder()
                .id(dailyDiscount.getId())
                .title(dailyDiscount.getTitle())
                .subtitle(dailyDiscount.getSubtitle())
                .description(dailyDiscount.getDescription())
                .imageUrl(dailyDiscount.getImageUrl())
                .redirectLink(dailyDiscount.getRedirectLink())
                .discountLabel(dailyDiscount.getDiscountLabel())
                .discountPercent(dailyDiscount.getDiscountPercent())
                .active(dailyDiscount.isActive())
                .highlighted(dailyDiscount.isHighlighted())
                .displayOrder(dailyDiscount.getDisplayOrder())
                .startDate(dailyDiscount.getStartDate())
                .endDate(dailyDiscount.getEndDate())
                .build();
    }

    private static Integer clampDiscount(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 1) {
            return 1;
        }
        if (value > 95) {
            return 95;
        }
        return value;
    }
}
