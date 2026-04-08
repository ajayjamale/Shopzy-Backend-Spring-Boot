package com.ajay.controller;

import com.ajay.domain.PaymentMethod;
import com.ajay.exception.UserException;
import com.ajay.model.*;
import com.ajay.repository.CartRepository;
import com.ajay.repository.PaymentOrderRepository;
import com.ajay.request.SettlementRequest;
import com.ajay.response.ApiResponse;
import com.ajay.response.PaymentLinkResponse;
import com.ajay.service.*;
import com.razorpay.PaymentLink;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final SellerReportService sellerReportService;
    private final SellerService sellerService;
    private final SettlementService settlementService;
    private final CartRepository cartRepository;
    private final PaymentOrderRepository paymentOrderRepository;


    @PostMapping("/api/payment/{paymentMethod}/order/{orderId}")
    public ResponseEntity<PaymentLinkResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        PaymentOrder order = paymentService.getPaymentOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!paymentMethod.equals(PaymentMethod.RAZORPAY)) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }

        PaymentLink payment = paymentService.createRazorpayPaymentLink(user, order.getAmount(), order.getId());
        String paymentUrl = payment.get("short_url");
        String paymentUrlId = payment.get("id");

        order.setPaymentMethod(paymentMethod);
        order.setPaymentLinkId(paymentUrlId);
        paymentOrderRepository.save(order);

        PaymentLinkResponse response = new PaymentLinkResponse();
        response.setPayment_link_url(paymentUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/api/payment/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentOrder paymentOrder= paymentService
                .getPaymentOrderByPaymentId(paymentLinkId);

        boolean paymentSuccess = paymentService.ProceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId
        );
        if(paymentSuccess){
            for(Order order:paymentOrder.getOrders()){
                transactionService.createTransaction(order);
                Seller seller=sellerService.getSellerById(order.getSellerId());
                SellerReport report=sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders()+1);
                report.setTotalEarnings(report.getTotalEarnings()+order.getTotalSellingPrice());
                report.setTotalSales(report.getTotalSales()+order.getOrderItems().size());
                sellerReportService.updateSellerReport(report);

                // Auto-create settlement entry per order ITEM (idempotent per orderItemId)
                order.getOrderItems().forEach(oi -> {
                    SettlementRequest settlementReq = new SettlementRequest();
                    settlementReq.setSellerId(order.getSellerId());
                    settlementReq.setOrderId(order.getId());
                    settlementReq.setOrderItemId(oi.getId());
                    settlementReq.setTransactionId(paymentId);
                    settlementReq.setOrderReference(order.getOrderId());
                    settlementReq.setGrossAmount(oi.getSellingPrice() != null ? (long) oi.getSellingPrice() : (long) oi.getMrpPrice());
                    settlementReq.setPaymentMethod(paymentOrder.getPaymentMethod() != null ? paymentOrder.getPaymentMethod().name() : "ONLINE");
                    settlementReq.setRemarks("Auto-created after successful payment (item " + oi.getId() + ")");
                    try {
                        settlementService.createSettlement(settlementReq);
                    } catch (Exception ex) {
                        // swallow duplicate/eligibility errors to avoid breaking payment success,
                        // but they should be visible in logs for ops follow-up
                        System.err.println("[Settlement] creation skipped for order item " + oi.getId() + ": " + ex.getMessage());
                    }
                });
            }
            Cart cart=cartRepository.findByUserId(user.getId());
            cart.setCouponPrice(0);
            cart.setCouponCode(null);
            cart.getCartItems().clear();
            cartRepository.save(cart);

        }
      
        ApiResponse res = new ApiResponse();
        res.setMessage("Payment successful");
        res.setStatus(true);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
}
