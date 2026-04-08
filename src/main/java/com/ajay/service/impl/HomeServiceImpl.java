package com.ajay.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ajay.domain.HomeCategorySection;
import com.ajay.model.Deal;
import com.ajay.model.Home;
import com.ajay.model.HomeCategory;
import com.ajay.repository.DealRepository;
import com.ajay.service.HomeService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;



    @Override
    public Home creatHomePageData(List<HomeCategory> allCategories) {


        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.GRID)
                .collect(Collectors.toList());

        List<HomeCategory> shopByCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.SHOP_BY_CATEGORIES)
                .collect(Collectors.toList());

        List<HomeCategory> electricCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.ELECTRIC_CATEGORIES)
                .collect(Collectors.toList());

        List<HomeCategory> dealCategories = allCategories.stream()
                .filter(category -> category.getSection() == HomeCategorySection.DEALS)
                .toList();

        List<Deal> createdDeals = new ArrayList<>();

        if (dealRepository.findAll().isEmpty()) {
            List<Deal> deals = allCategories.stream()
                    .filter(category -> category.getSection() == HomeCategorySection.DEALS)
                    .map(category -> {
                        Deal d = new Deal();
                        d.setCategory(category);
                        d.setTitle(category.getName());
                        d.setSubtitle("Limited time offer");
                        d.setDescription("Save big on " + category.getName());
                        d.setDiscount(10);
                        d.setDiscountLabel("10% OFF");
                        d.setActive(true);
                        d.setFeatured(true);
                        d.setDisplayOrder(0);
                        d.setStartDate(java.time.LocalDate.now());
                        d.setEndDate(java.time.LocalDate.now().plusDays(30));
                        return d;
                    })  // Assuming a discount of 10 for each deal
                    .collect(Collectors.toList());
            createdDeals = dealRepository.saveAll(deals);
        } else createdDeals = dealRepository.findAll();


        Home home = new Home();
        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDeals(createdDeals);
        home.setDealCategories(dealCategories);

        return home;
    }


}
