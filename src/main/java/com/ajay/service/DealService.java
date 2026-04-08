package com.ajay.service;

import java.util.List;

import com.ajay.model.Deal;

public interface DealService {
    Deal createDeal(Deal deal);
//    List<Deal> createDeals(List<Deal> deals);
    List<Deal> getDeals(boolean onlyActive);
    Deal updateDeal(Deal deal,Long id) throws Exception;
    void deleteDeal(Long id) throws Exception;

    Deal toggleActive(Long id, boolean active) throws Exception;
}
