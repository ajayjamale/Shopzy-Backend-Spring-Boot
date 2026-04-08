package com.ajay.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ajay.model.Deal;
import com.ajay.model.Home;
import com.ajay.model.HomeCategory;
import com.ajay.repository.DealRepository;
import com.ajay.repository.HomeCategoryRepository;
import com.ajay.service.DealService;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealRepository dealRepository;
    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public Deal createDeal(Deal deal) {
        HomeCategory category = homeCategoryRepository
                .findById(deal.getCategory().getId()).orElse(null);
        deal.setCategory(category);
        if (deal.getStartDate() == null) deal.setStartDate(LocalDate.now());
        if (deal.getEndDate() == null) deal.setEndDate(LocalDate.now().plusDays(30));
        return dealRepository.save(deal);
    }
//
//    @Override
//    public List<Deal> createDeals(List<Deal> deals) {
//        if(dealRepository.findAll().isEmpty()){
//            return dealRepository.saveAll(deals);
//        }
//        else return dealRepository.findAll();
//
//    }


    @Override
    public List<Deal> getDeals(boolean onlyActive) {
        if (onlyActive) {
            LocalDate today = LocalDate.now();
            return dealRepository.findByActiveTrueAndFeaturedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAscIdAsc(today, today);
        }
        return dealRepository.findAll();
    }

    @Override
    public Deal updateDeal(Deal deal,Long id) throws Exception {
        Deal existingDeal = dealRepository.findById(id).orElse(null);
        HomeCategory category=homeCategoryRepository.findById(deal.getCategory().getId()).orElse(null);

        if(existingDeal!=null){
            if(deal.getDiscount()!=null){
                existingDeal.setDiscount(deal.getDiscount());
            }
            if(deal.getTitle()!=null) existingDeal.setTitle(deal.getTitle());
            if(deal.getSubtitle()!=null) existingDeal.setSubtitle(deal.getSubtitle());
            if(deal.getDescription()!=null) existingDeal.setDescription(deal.getDescription());
            if(deal.getImage()!=null) existingDeal.setImage(deal.getImage());
            if(deal.getDiscountLabel()!=null) existingDeal.setDiscountLabel(deal.getDiscountLabel());
            if(deal.getRedirectLink()!=null) existingDeal.setRedirectLink(deal.getRedirectLink());
            if(deal.getDisplayOrder()!=null) existingDeal.setDisplayOrder(deal.getDisplayOrder());
            if(deal.getStartDate()!=null) existingDeal.setStartDate(deal.getStartDate());
            if(deal.getEndDate()!=null) existingDeal.setEndDate(deal.getEndDate());
            if(deal.getActive()!=null) existingDeal.setActive(deal.getActive());
            if(deal.getFeatured()!=null) existingDeal.setFeatured(deal.getFeatured());
            if(category!=null){
                existingDeal.setCategory(category);
            }
            return dealRepository.save(existingDeal);
        }
        throw new Exception("Deal not found");
    }

    @Override
    public void deleteDeal(Long id) throws Exception {
        Deal deal = dealRepository.findById(id).orElse(null);

        if (deal != null) {

            dealRepository.delete(deal);
        }

    }

    @Override
    public Deal toggleActive(Long id, boolean active) throws Exception {
        Deal deal = dealRepository.findById(id).orElseThrow(() -> new Exception("Deal not found"));
        deal.setActive(active);
        return dealRepository.save(deal);
    }


}
