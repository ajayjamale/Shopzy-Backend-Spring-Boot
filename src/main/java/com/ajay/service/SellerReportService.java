package com.ajay.service;

import com.ajay.model.Seller;
import com.ajay.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport( SellerReport sellerReport);

}

