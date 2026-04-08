package com.ajay.controller;

import com.ajay.domains.SettlementStatus;
import com.ajay.exception.OrderException;
import com.ajay.exception.SettlementException;
import com.ajay.exception.SellerException;
import com.ajay.model.Seller;
import com.ajay.payload.response.SettlementResponse;
import com.ajay.payload.response.SettlementSummaryResponse;
import com.ajay.service.SellerService;
import com.ajay.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ajay.payload.request.SettlementRequest;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(
            @Valid @RequestBody SettlementRequest request,
            @RequestHeader(value = "Authorization", required = false) String jwt
    )
            throws SettlementException, OrderException, SellerException {
        if (request.getSellerId() == null && jwt != null) {
            Seller seller = sellerService.getSellerProfile(jwt);
            request.setSellerId(seller.getId());
        }
        return ResponseEntity.ok(settlementService.createSettlement(request));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestHeader(value = "Authorization", required = false) String jwt
    ) {
        Long resolvedSellerId = sellerId;
        try {
            if (resolvedSellerId == null && jwt != null) {
                resolvedSellerId = sellerService.getSellerProfile(jwt).getId();
            }
        } catch (SellerException ignored) {}

        List<SettlementResponse> items = settlementService.getSettlements(resolvedSellerId, status,
                parseDate(fromDate), parseDate(toDate));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable Long id) throws SettlementException {
        return ResponseEntity.ok(settlementService.getSettlement(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SettlementResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest body
    ) throws SettlementException {
        return ResponseEntity.ok(settlementService.updateStatus(id, body.status(), body.remarks()));
    }

    @GetMapping("/summary")
    public ResponseEntity<SettlementSummaryResponse> getSummary(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestHeader(value = "Authorization", required = false) String jwt
    ) {
        Long resolvedSellerId = sellerId;
        try {
            if (resolvedSellerId == null && jwt != null) {
                resolvedSellerId = sellerService.getSellerProfile(jwt).getId();
            }
        } catch (SellerException ignored) {}
        return ResponseEntity.ok(settlementService.getSummary(resolvedSellerId, status, parseDate(fromDate), parseDate(toDate)));
    }

    public record StatusUpdateRequest(SettlementStatus status, String remarks) {}

    private LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception ignored) {}
        try {
            return java.time.LocalDate.parse(dateString).atStartOfDay();
        } catch (Exception ignored) {}
        return null;
    }
}
