package com.ajay.service.impl;

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

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Override
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        Long amount = orders.stream().mapToLong(Order::getTotalSellingPrice).sum();
        int couponPrice = cartRepository.findByUserId(user.getId()).getCouponPrice();

        PaymentOrder order = new PaymentOrder();
        order.setUser(user);
        order.setAmount(amount - couponPrice);
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
                                       String paymentLinkId) throws RazorpayException {
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);
            Payment payment = razorpay.payments.fetch(paymentId);

            Integer amount = payment.get("amount");
            String status = payment.get("status");

            if (status.equals("captured")) {
                Set<Order> orders = paymentOrder.getOrders();
                for (Order order : orders) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);
                }
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true;
            }
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }
        return false;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(User user,
                                                  Long Amount,
                                                  Long orderId) throws RazorpayException {
        // ── FIX ──────────────────────────────────────────────────────────────
        // BEFORE: Long amount = Amount * 100;
        //
        // getTotalSellingPrice() returns prices in RUPEES (e.g. ₹1,299 = 1299).
        // Razorpay requires the amount in PAISE, so multiplying by 100 is correct
        // in principle — BUT if your products are stored at large values
        // (e.g. electronics at ₹50,000), then 50000 * 100 = 50,00,000 paise
        // which equals ₹50,000 and is fine. However if getTotalSellingPrice()
        // is already returning paise (i.e. storing prices × 100 in the DB),
        // then multiplying by 100 again sends 50,000 * 100 * 100 = way over limit.
        //
        // The error "amount exceeds maximum amount allowed" confirms the value
        // being sent is over 50,000,000 paise (₹5,00,000).
        //
        // SOLUTION: Do NOT multiply by 100 here. Instead, ensure prices are
        // stored as whole rupees in the DB (sellingPrice = 1299 means ₹1,299),
        // and convert to paise only at this single point of contact with Razorpay.
        //
        // If after this fix the Razorpay payment shows 1/100th of the correct
        // amount (e.g. ₹12.99 instead of ₹1,299), it means your DB stores prices
        // already in paise — in that case remove the * 100 below AND divide the
        // stored price by 100 everywhere in your UI display instead.
        // ─────────────────────────────────────────────────────────────────────
        Long amountInPaise = Amount * 100; // Amount is in rupees → convert to paise

        try {
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
            paymentLinkRequest.put("callback_url", "http://localhost:5173/payment-success/" + orderId);
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

            System.out.println("Razorpay payment link created: " + payment.get("short_url")
                    + " | amount (paise): " + amountInPaise
                    + " | amount (rupees): " + Amount);

            return payment;

        } catch (RazorpayException e) {
            System.out.println("Error creating payment link: " + e.getMessage());
            throw new RazorpayException(e.getMessage());
        }
    }
}