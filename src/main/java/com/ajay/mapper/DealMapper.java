package com.ajay.mapper;

import com.ajay.model.Deal;
import com.ajay.payload.response.DealResponse;

public class DealMapper {

    private DealMapper() {
    }

    public static Deal toEntity(DealResponse response) {
        if (response == null) {
            return null;
        }
        Deal deal = new Deal();
        deal.setId(response.getId());
        deal.setTitle(response.getTitle());
        deal.setSubtitle(response.getSubtitle());
        deal.setDescription(response.getDescription());
        deal.setImage(response.getImage());
        deal.setDiscountLabel(response.getDiscountLabel());
        deal.setRedirectLink(response.getRedirectLink());
        deal.setDiscount(response.getDiscount());
        deal.setDisplayOrder(response.getDisplayOrder());
        deal.setStartDate(response.getStartDate());
        deal.setEndDate(response.getEndDate());
        return deal;
    }

    public static Deal updateEntity(Deal deal, DealResponse response) {
        if (deal == null || response == null) {
            return deal;
        }
        if (response.getTitle() != null) {
            deal.setTitle(response.getTitle());
        }
        if (response.getSubtitle() != null) {
            deal.setSubtitle(response.getSubtitle());
        }
        if (response.getDescription() != null) {
            deal.setDescription(response.getDescription());
        }
        if (response.getImage() != null) {
            deal.setImage(response.getImage());
        }
        if (response.getDiscountLabel() != null) {
            deal.setDiscountLabel(response.getDiscountLabel());
        }
        if (response.getRedirectLink() != null) {
            deal.setRedirectLink(response.getRedirectLink());
        }
        if (response.getDiscount() != null) {
            deal.setDiscount(response.getDiscount());
        }
        if (response.getDisplayOrder() != null) {
            deal.setDisplayOrder(response.getDisplayOrder());
        }
        if (response.getStartDate() != null) {
            deal.setStartDate(response.getStartDate());
        }
        if (response.getEndDate() != null) {
            deal.setEndDate(response.getEndDate());
        }
        return deal;
    }

    public static DealResponse toResponse(Deal deal) {
        if (deal == null) {
            return null;
        }
        return DealResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .subtitle(deal.getSubtitle())
                .description(deal.getDescription())
                .image(deal.getImage())
                .discountLabel(deal.getDiscountLabel())
                .redirectLink(deal.getRedirectLink())
                .discount(deal.getDiscount())
                .displayOrder(deal.getDisplayOrder())
                .startDate(deal.getStartDate())
                .endDate(deal.getEndDate())
                .build();
    }
}
