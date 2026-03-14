package com.ajay.service;

import java.util.List;
import java.util.Optional;

import com.ajay.model.Seller;
import com.ajay.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport( SellerReport sellerReport);

}
