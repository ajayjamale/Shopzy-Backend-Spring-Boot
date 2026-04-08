package com.ajay.service;

import com.ajay.payload.request.DailyDiscountRequest;
import com.ajay.payload.response.DailyDiscountResponse;

import java.util.List;

public interface DailyDiscountService {
    DailyDiscountResponse create(DailyDiscountRequest request);

    DailyDiscountResponse update(Long id, DailyDiscountRequest request);

    void delete(Long id);

    DailyDiscountResponse updateStatus(Long id, boolean active);

    List<DailyDiscountResponse> getAll(boolean onlyActive);
}
