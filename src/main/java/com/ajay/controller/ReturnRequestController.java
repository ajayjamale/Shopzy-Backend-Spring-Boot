package com.ajay.controller;

import com.ajay.exception.OrderException;
import com.ajay.exception.SellerException;
import com.ajay.exception.UserException;
import com.ajay.model.ReturnRequest;
import com.ajay.model.Seller;
import com.ajay.model.User;
import com.ajay.payload.request.CreateReturnRequest;
import com.ajay.payload.request.UpdateReturnStatusRequest;
import com.ajay.service.ReturnRequestService;
import com.ajay.service.SellerService;
import com.ajay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final UserService userService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ReturnRequest> createReturnRequest(
            @Valid @RequestBody CreateReturnRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws UserException, OrderException {
        User user = userService.findUserProfileByJwt(jwt);
        ReturnRequest created = returnRequestService.createReturnRequest(request, user);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequest>> getReturnRequests(
            @RequestHeader("Authorization") String jwt
    ) throws UserException, SellerException {
        if (isRole("ROLE_ADMIN")) {
            return ResponseEntity.ok(returnRequestService.getAllReturnRequests());
        }
        if (isRole("ROLE_SELLER")) {
            Seller seller = sellerService.getSellerProfile(jwt);
            return ResponseEntity.ok(returnRequestService.getReturnRequestsForSeller(seller.getId()));
        }
        User user = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(returnRequestService.getReturnRequestsForUser(user.getId()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReturnRequest> updateReturnStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnStatusRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws UserException, SellerException, OrderException {
        boolean isAdmin = isRole("ROLE_ADMIN");
        boolean isSeller = isRole("ROLE_SELLER");
        if (!isAdmin && !isSeller) {
            throw new OrderException("only admin or seller can update return status");
        }

        Long sellerId = null;
        if (!isAdmin) {
            sellerId = sellerService.getSellerProfile(jwt).getId();
        }

        ReturnRequest updated = returnRequestService.updateStatus(
                id,
                request.getStatus(),
                request.getAdminComment(),
                sellerId,
                isAdmin
        );
        return ResponseEntity.ok(updated);
    }

    private boolean isRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}

