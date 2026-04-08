package com.ajay.service;

import com.ajay.domains.SettlementStatus;
import com.ajay.exception.OrderException;
import com.ajay.exception.SettlementException;
import com.ajay.exception.SellerException;
import com.ajay.model.Settlement;
import com.ajay.payload.request.SettlementRequest;
import com.ajay.payload.response.SettlementResponse;
import com.ajay.payload.response.SettlementSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementService {

    SettlementResponse createSettlement(SettlementRequest request) throws SettlementException, OrderException, SellerException;

    SettlementResponse getSettlement(Long id) throws SettlementException;

    List<SettlementResponse> getSettlements(Long sellerId,
                                            SettlementStatus status,
                                            LocalDateTime from,
                                            LocalDateTime to);

    SettlementResponse updateStatus(Long id, SettlementStatus status, String remarks) throws SettlementException;

    SettlementSummaryResponse getSummary(Long sellerId,
                                         SettlementStatus status,
                                         LocalDateTime from,
                                         LocalDateTime to);

    Settlement toEntity(SettlementRequest request) throws SellerException;
}

