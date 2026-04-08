package com.ajay.service.impl;

import com.ajay.domain.OrderStatus;
import com.ajay.domain.PaymentOrderStatus;
import com.ajay.domain.PaymentStatus;
import com.ajay.model.Order;
import com.ajay.model.PaymentOrder;
import com.ajay.model.User;
import com.ajay.repository.CartRepository;
import com.ajay.repository.OrderRepository;
import com.ajay.repository.PaymentOrderRepository;
import com.ajay.service.PaymentService;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Override
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        long amount = orders.stream().mapToLong(Order::getTotalSellingPrice).sum();
        int couponPrice = cartRepository.findByUserId(user.getId()).getCouponPrice();
        long payableAmount = Math.max(0L, amount - couponPrice);

        PaymentOrder order = new PaymentOrder();
        order.setUser(user);
        order.setAmount(payableAmount);
        order.setOrders(orders);

        return paymentOrderRepository.save(order);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        Optional<PaymentOrder> optionalPaymentOrder = paymentOrderRepository.findById(id);
        if (optionalPaymentOrder.isEmpty()) {
            throw new Exception("payment order not found with id " + id);
        }
        return optionalPaymentOrder.get();
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception {
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(paymentLinkId);
        if (paymentOrder == null) {
            throw new Exception("payment order not found with id " + paymentLinkId);
        }
        return paymentOrder;
    }

    @Override
    public Boolean ProceedPaymentOrder(PaymentOrder paymentOrder,
                                       String paymentId,
                                       String paymentReferenceId) throws RazorpayException {
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.SUCCESS)) {
            return true;
        }
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.FAILED)) {
            return false;
        }

        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);
        Payment payment = razorpay.payments.fetch(paymentId);
        String status = payment.get("status");

        String fetchedRazorpayOrderId = null;
        if (payment.has("order_id")) {
            fetchedRazorpayOrderId = payment.get("order_id");
        }

        if (paymentOrder.getRazorpayOrderId() != null
                && fetchedRazorpayOrderId != null
                && !paymentOrder.getRazorpayOrderId().equals(fetchedRazorpayOrderId)) {
            failPaymentOrder(paymentOrder);
            return false;
        }

        if ("captured".equalsIgnoreCase(status)) {
            Set<Order> orders = paymentOrder.getOrders();
            for (Order order : orders) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                orderRepository.save(order);
            }
            paymentOrder.setRazorpayPaymentId(paymentId);
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }

        failPaymentOrder(paymentOrder);
        return false;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(User user,
                                                 Long amount,
                                                 Long orderId) throws RazorpayException {
        long amountInPaise = Math.max(0L, amount) * 100;

        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", amountInPaise);
        paymentLinkRequest.put("currency", "INR");

        JSONObject customer = new JSONObject();
        customer.put("name", user.getFullName());
        customer.put("email", user.getEmail());
        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);

        paymentLinkRequest.put("reminder_enable", true);
        paymentLinkRequest.put("callback_url", frontendUrl + "/payment-success/" + orderId);
        paymentLinkRequest.put("callback_method", "get");

        return razorpay.paymentLink.create(paymentLinkRequest);
    }

    @Override
    public com.razorpay.Order createRazorpayOrder(User user, Long amount, Long paymentOrderId) throws RazorpayException {
        long amountInPaise = Math.max(0L, amount) * 100;
        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

        JSONObject request = new JSONObject();
        request.put("amount", amountInPaise);
        request.put("currency", "INR");
        request.put("receipt", "po_" + paymentOrderId);
        request.put("payment_capture", 1);

        JSONObject notes = new JSONObject();
        notes.put("payment_order_id", String.valueOf(paymentOrderId));
        notes.put("customer_id", String.valueOf(user.getId()));
        request.put("notes", notes);

        return razorpay.orders.create(request);
    }

    @Override
    public boolean isValidRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            return false;
        }
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] digest = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }

            return hex.toString().equalsIgnoreCase(razorpaySignature.trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void failPaymentOrder(PaymentOrder paymentOrder) {
        Set<Order> orders = paymentOrder.getOrders();
        for (Order order : orders) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            if (order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.CANCELLED);
            }
            orderRepository.save(order);
        }

        paymentOrder.setStatus(PaymentOrderStatus.FAILED);
        paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public String getRazorpayKey() {
        return apiKey;
    }
}
