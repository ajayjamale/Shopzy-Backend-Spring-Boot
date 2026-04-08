package com.ajay.controller;

import com.ajay.domains.PaymentMethod;
import com.ajay.domains.PaymentOrderStatus;
import com.ajay.model.Cart;
import com.ajay.model.Order;
import com.ajay.model.PaymentOrder;
import com.ajay.model.Seller;
import com.ajay.model.SellerReport;
import com.ajay.model.User;
import com.ajay.repository.CartRepository;
import com.ajay.repository.PaymentOrderRepository;
import com.ajay.payload.request.PaymentFailureRequest;
import com.ajay.payload.request.RazorpayVerifyPaymentRequest;
import com.ajay.payload.request.SettlementRequest;
import com.ajay.payload.response.ApiResponse;
import com.ajay.payload.response.PaymentLinkResponse;
import com.ajay.service.PaymentService;
import com.ajay.service.SellerReportService;
import com.ajay.service.SellerService;
import com.ajay.service.SettlementService;
import com.ajay.service.TransactionService;
import com.ajay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

        com.razorpay.Order razorpayOrder = paymentService.createRazorpayOrder(user, order.getAmount(), order.getId());
        String razorpayOrderId = razorpayOrder.get("id");
        Long amountInPaise = toLongValue(razorpayOrder.get("amount"), "amount");
        String currency = razorpayOrder.get("currency");

        order.setPaymentMethod(paymentMethod);
        order.setRazorpayOrderId(razorpayOrderId);
        paymentOrderRepository.save(order);

        PaymentLinkResponse response = new PaymentLinkResponse();
        response.setPayment_order_id(order.getId());
        response.setRazorpay_order_id(razorpayOrderId);
        response.setRazorpay_key(paymentService.getRazorpayKey());
        response.setAmount(amountInPaise);
        response.setCurrency(currency);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private Long toLongValue(Object value, String fieldName) {
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ignored) {
                // Fall through to throw a uniform error below.
            }
        }
        throw new IllegalStateException("Unexpected type for Razorpay field '" + fieldName + "'");
    }

    @PostMapping("/api/payment/verify")
    public ResponseEntity<ApiResponse> verifyRazorpayPayment(
            @Valid @RequestBody RazorpayVerifyPaymentRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(request.getPaymentOrderId());
        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (paymentOrder.getRazorpayOrderId() != null
                && !paymentOrder.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {
            paymentService.failPaymentOrder(paymentOrder);
            return paymentFailedResponse("Payment order mismatch.");
        }

        boolean signatureValid = paymentService.isValidRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );
        if (!signatureValid) {
            paymentService.failPaymentOrder(paymentOrder);
            return paymentFailedResponse("Invalid payment signature.");
        }

        boolean wasPending = paymentOrder.getStatus() == PaymentOrderStatus.PENDING;
        boolean paymentSuccess = paymentService.ProceedPaymentOrder(
                paymentOrder,
                request.getRazorpayPaymentId(),
                request.getRazorpayOrderId()
        );

        if (!paymentSuccess) {
            return paymentFailedResponse("Payment not captured.");
        }

        if (wasPending) {
            finalizeSuccessfulPayment(paymentOrder, user, request.getRazorpayPaymentId());
        }

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment verified successfully");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/api/payment/fail")
    public ResponseEntity<ApiResponse> markPaymentFailed(
            @Valid @RequestBody PaymentFailureRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(request.getPaymentOrderId());

        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (paymentOrder.getStatus() == PaymentOrderStatus.PENDING) {
            paymentService.failPaymentOrder(paymentOrder);
        }

        ApiResponse res = new ApiResponse();
        res.setStatus(true);
        res.setMessage(request.getReason() == null || request.getReason().isBlank()
                ? "Payment marked as failed"
                : "Payment marked as failed: " + request.getReason());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/payment/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentOrder paymentOrder = paymentService.getPaymentOrderByPaymentId(paymentLinkId);
        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        boolean wasPending = paymentOrder.getStatus() == PaymentOrderStatus.PENDING;
        boolean paymentSuccess = paymentService.ProceedPaymentOrder(paymentOrder, paymentId, paymentLinkId);
        if (!paymentSuccess) {
            return paymentFailedResponse("Payment not captured.");
        }

        if (wasPending) {
            finalizeSuccessfulPayment(paymentOrder, user, paymentId);
        }

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment successful");
        res.setStatus(true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private ResponseEntity<ApiResponse> paymentFailedResponse(String message) {
        ApiResponse res = new ApiResponse();
        res.setStatus(false);
        res.setMessage(message);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    private void finalizeSuccessfulPayment(PaymentOrder paymentOrder, User user, String transactionId) throws Exception {
        for (Order order : paymentOrder.getOrders()) {
            transactionService.createTransaction(order);
            Seller seller = sellerService.getSellerById(order.getSellerId());
            SellerReport report = sellerReportService.getSellerReport(seller);
            report.setTotalOrders(report.getTotalOrders() + 1);
            report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
            report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
            sellerReportService.updateSellerReport(report);

            // Auto-create settlement entry per order item (idempotent per orderItemId)
            order.getOrderItems().forEach(oi -> {
                SettlementRequest settlementReq = new SettlementRequest();
                settlementReq.setSellerId(order.getSellerId());
                settlementReq.setOrderId(order.getId());
                settlementReq.setOrderItemId(oi.getId());
                settlementReq.setTransactionId(transactionId);
                settlementReq.setOrderReference(order.getOrderId());
                settlementReq.setGrossAmount(oi.getSellingPrice() != null ? (long) oi.getSellingPrice() : (long) oi.getMrpPrice());
                settlementReq.setPaymentMethod(paymentOrder.getPaymentMethod() != null ? paymentOrder.getPaymentMethod().name() : "ONLINE");
                settlementReq.setRemarks("Auto-created after successful payment (item " + oi.getId() + ")");
                try {
                    settlementService.createSettlement(settlementReq);
                } catch (Exception ex) {
                    // swallow duplicate/eligibility errors to avoid breaking payment success
                    System.err.println("[Settlement] creation skipped for order item " + oi.getId() + ": " + ex.getMessage());
                }
            });
        }

        Cart cart = cartRepository.findByUserId(user.getId());
        cart.setCouponPrice(0);
        cart.setCouponCode(null);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}

