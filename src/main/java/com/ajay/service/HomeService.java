package com.ajay.service;

import java.util.List;

import com.ajay.model.Home;
import com.ajay.model.HomeCategory;

public interface HomeService {

    Home creatHomePageData(List<HomeCategory> categories);

}
