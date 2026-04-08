package com.ajay.service.impl;

import com.ajay.domains.OrderStatus;
import com.ajay.domains.ReturnStatus;
import com.ajay.domains.SettlementStatus;
import com.ajay.exception.OrderException;
import com.ajay.model.Order;
import com.ajay.model.OrderItem;
import com.ajay.model.ReturnRequest;
import com.ajay.model.SellerReport;
import com.ajay.model.User;
import com.ajay.repository.OrderRepository;
import com.ajay.repository.ReturnRequestRepository;
import com.ajay.repository.SellerReportRepository;
import com.ajay.repository.SettlementRepository;
import com.ajay.payload.request.CreateReturnRequest;
import com.ajay.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private static final int RETURN_WINDOW_DAYS = 7;

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;
    private final SellerReportRepository sellerReportRepository;

    @Override
    @Transactional
    public ReturnRequest createReturnRequest(CreateReturnRequest request, User user) throws OrderException {
        if (request.getOrderId() == null || request.getOrderItemId() == null) {
            throw new OrderException("orderId and orderItemId are required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new OrderException("quantity should be at least 1");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new OrderException("return reason is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderException("order not found: " + request.getOrderId()));

        if (order.getUser() == null || order.getUser().getId() == null || !order.getUser().getId().equals(user.getId())) {
            throw new OrderException("you can't request return for this order");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED
                && order.getOrderStatus() != OrderStatus.RETURN_REQUESTED
                && order.getOrderStatus() != OrderStatus.REFUND_INITIATED) {
            throw new OrderException("return is allowed only for delivered orders");
        }

        if (order.getDeliverDate() == null
                || ChronoUnit.DAYS.between(order.getDeliverDate(), LocalDateTime.now()) > RETURN_WINDOW_DAYS) {
            throw new OrderException("return window has expired");
        }

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new OrderException("order items not found in this order");
        }

        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> request.getOrderItemId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new OrderException("order item not found in this order"));

        if (request.getQuantity() > orderItem.getQuantity()) {
            throw new OrderException("requested quantity exceeds purchased quantity");
        }

        Set<ReturnStatus> openStatuses = Set.of(
                ReturnStatus.REQUESTED,
                ReturnStatus.APPROVED,
                ReturnStatus.PICKUP_SCHEDULED,
                ReturnStatus.RECEIVED,
                ReturnStatus.REFUND_INITIATED
        );
        Optional<ReturnRequest> existing = returnRequestRepository
                .findFirstByOrderItemIdAndUserIdAndStatusIn(orderItem.getId(), user.getId(), openStatuses);
        if (existing.isPresent()) {
            throw new OrderException("return request already exists for this order item");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId(order.getId());
        returnRequest.setOrderItemId(orderItem.getId());
        returnRequest.setUserId(user.getId());
        returnRequest.setSellerId(order.getSellerId());
        returnRequest.setQuantity(request.getQuantity());
        returnRequest.setReason(request.getReason().trim());
        returnRequest.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        returnRequest.setImages(request.getImages() == null ? List.of() : request.getImages());
        returnRequest.setStatus(ReturnStatus.REQUESTED);

        order.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        return returnRequestRepository.save(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> getReturnRequestsForUser(Long userId) {
        return returnRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> getReturnRequestsForSeller(Long sellerId) {
        return returnRequestRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> getAllReturnRequests() {
        return returnRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public ReturnRequest updateStatus(Long id,
                                      ReturnStatus status,
                                      String adminComment,
                                      Long sellerId,
                                      boolean isAdmin) throws OrderException {
        if (status == null) {
            throw new OrderException("status is required");
        }

        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new OrderException("return request not found: " + id));

        if (!isAdmin) {
            if (sellerId == null || !sellerId.equals(request.getSellerId())) {
                throw new OrderException("you can't update this return request");
            }
        }

        ReturnStatus previousStatus = request.getStatus();
        request.setStatus(status);
        if (adminComment != null) {
            request.setAdminComment(adminComment);
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderException("order not found for return request"));

        if (status == ReturnStatus.REJECTED) {
            order.setOrderStatus(OrderStatus.DELIVERED);
        } else if (status == ReturnStatus.REFUND_INITIATED) {
            order.setOrderStatus(OrderStatus.REFUND_INITIATED);
        } else if (status == ReturnStatus.REFUNDED) {
            order.setOrderStatus(OrderStatus.RETURNED);
            settlementRepository.findByOrderItemId(request.getOrderItemId()).ifPresent(settlement -> {
                settlement.setSettlementStatus(SettlementStatus.CANCELLED);
                settlementRepository.save(settlement);
            });
        } else if (status == ReturnStatus.APPROVED
                || status == ReturnStatus.PICKUP_SCHEDULED
                || status == ReturnStatus.RECEIVED) {
            order.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        }

        orderRepository.save(order);
        syncSellerRefundTotals(request, order, previousStatus, status);
        return returnRequestRepository.save(request);
    }

    private void syncSellerRefundTotals(ReturnRequest request,
                                        Order order,
                                        ReturnStatus previousStatus,
                                        ReturnStatus nextStatus) {
        if (request.getSellerId() == null) {
            return;
        }
        if (previousStatus == nextStatus) {
            return;
        }

        long refundAmount = calculateRefundAmount(order, request);
        if (refundAmount <= 0) {
            return;
        }

        SellerReport report = sellerReportRepository.findBySellerId(request.getSellerId());
        if (report == null) {
            return;
        }

        long currentRefunds = report.getTotalRefunds() == null ? 0L : report.getTotalRefunds();

        if (previousStatus != ReturnStatus.REFUNDED && nextStatus == ReturnStatus.REFUNDED) {
            report.setTotalRefunds(currentRefunds + refundAmount);
            sellerReportRepository.save(report);
            return;
        }

        if (previousStatus == ReturnStatus.REFUNDED && nextStatus != ReturnStatus.REFUNDED) {
            report.setTotalRefunds(Math.max(0L, currentRefunds - refundAmount));
            sellerReportRepository.save(report);
        }
    }

    private long calculateRefundAmount(Order order, ReturnRequest request) {
        if (request.getOrderItemId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            return 0L;
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return 0L;
        }

        return order.getOrderItems().stream()
                .filter(item -> request.getOrderItemId().equals(item.getId()))
                .findFirst()
                .map(this::resolveOrderItemPrice)
                .map(price -> price * request.getQuantity())
                .orElse(0L);
    }

    private long resolveOrderItemPrice(OrderItem item) {
        if (item.getSellingPrice() != null && item.getSellingPrice() > 0) {
            return item.getSellingPrice();
        }
        if (item.getMrpPrice() != null && item.getMrpPrice() > 0) {
            return item.getMrpPrice();
        }
        return 0L;
    }
}
