package com.ajay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ajay.model.Home;
import com.ajay.model.HomeCategory;
import com.ajay.response.HomePageResponse;
import com.ajay.service.HomeContentService;
import com.ajay.service.HomeCategoryService;
import com.ajay.service.HomeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;
    private final HomeContentService homeContentService;

    @GetMapping("/home-page")
    public ResponseEntity<HomePageResponse> getHomePageData() {
        return ResponseEntity.ok(homeContentService.getPublicHomePage());
    }

    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories
    ) {
        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategories);
        Home home=homeService.creatHomePageData(categories);
        return ResponseEntity.ok(home);
    }
}
