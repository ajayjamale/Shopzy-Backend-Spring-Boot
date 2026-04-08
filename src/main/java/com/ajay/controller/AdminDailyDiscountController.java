package com.ajay.controller;

import com.ajay.payload.request.DailyDiscountRequest;
import com.ajay.payload.response.ApiResponse;
import com.ajay.payload.response.DailyDiscountResponse;
import com.ajay.service.DailyDiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/daily-discounts")
public class AdminDailyDiscountController {

    private final DailyDiscountService dailyDiscountService;

    @PostMapping
    public ResponseEntity<DailyDiscountResponse> create(@Valid @RequestBody DailyDiscountRequest request) {
        return new ResponseEntity<>(dailyDiscountService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DailyDiscountResponse>> getAll(
            @RequestParam(defaultValue = "false") boolean onlyActive
    ) {
        return ResponseEntity.ok(dailyDiscountService.getAll(onlyActive));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DailyDiscountResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DailyDiscountRequest request
    ) {
        return ResponseEntity.ok(dailyDiscountService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DailyDiscountResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(dailyDiscountService.updateStatus(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        dailyDiscountService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Daily discount deleted", true));
    }
}
