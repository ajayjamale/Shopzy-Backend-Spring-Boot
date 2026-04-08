package com.ajay.service;

import com.ajay.model.Order;
import com.ajay.model.PaymentOrder;
import com.ajay.model.User;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;

import java.util.Set;

public interface PaymentService {

    PaymentOrder createOrder(User user, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long id) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception;

    Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentReferenceId) throws RazorpayException;

    PaymentLink createRazorpayPaymentLink(User user, Long amount, Long orderId) throws RazorpayException;

    com.razorpay.Order createRazorpayOrder(User user, Long amount, Long paymentOrderId) throws RazorpayException;

    boolean isValidRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);

    void failPaymentOrder(PaymentOrder paymentOrder);

    String getRazorpayKey();
}

