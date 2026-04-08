package com.ajay.service;

import com.ajay.domains.ReturnStatus;
import com.ajay.exception.OrderException;
import com.ajay.model.ReturnRequest;
import com.ajay.model.User;
import com.ajay.payload.request.CreateReturnRequest;

import java.util.List;

public interface ReturnRequestService {

    ReturnRequest createReturnRequest(CreateReturnRequest request, User user) throws OrderException;

    List<ReturnRequest> getReturnRequestsForUser(Long userId);

    List<ReturnRequest> getReturnRequestsForSeller(Long sellerId);

    List<ReturnRequest> getAllReturnRequests();

    ReturnRequest updateStatus(Long id,
                               ReturnStatus status,
                               String adminComment,
                               Long sellerId,
                               boolean isAdmin) throws OrderException;
}

