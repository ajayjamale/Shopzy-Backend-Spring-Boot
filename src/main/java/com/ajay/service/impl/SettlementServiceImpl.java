package com.ajay.service.impl;

import com.ajay.domains.OrderStatus;
import com.ajay.domains.PaymentStatus;
import com.ajay.domains.SettlementStatus;
import com.ajay.exception.OrderException;
import com.ajay.exception.SettlementException;
import com.ajay.exception.SellerException;
import com.ajay.model.Order;
import com.ajay.model.Seller;
import com.ajay.model.Settlement;
import com.ajay.repository.OrderRepository;
import com.ajay.repository.SellerRepository;
import com.ajay.repository.SettlementRepository;
import com.ajay.payload.request.SettlementRequest;
import com.ajay.payload.response.SettlementResponse;
import com.ajay.payload.response.SettlementSummaryResponse;
import com.ajay.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    private static final double DEFAULT_COMMISSION_RATE = 0.10; // 10%
    private static final double DEFAULT_PLATFORM_FEE_RATE = 0.02; // 2%
    private static final double DEFAULT_TAX_RATE = 0.00; // set to 0, tax included elsewhere

    @Override
    @Transactional
    public SettlementResponse createSettlement(SettlementRequest request) throws SettlementException, OrderException, SellerException {
        Settlement settlement = toEntity(request);

        // Ensure no duplicate for same order item (preferred) or order fallback
        if (settlement.getOrderItemId() != null) {
            Optional<Settlement> existingItem = settlementRepository.findByOrderItemId(settlement.getOrderItemId());
            if (existingItem.isPresent()) {
                throw new SettlementException("Settlement already exists for order item " + settlement.getOrderItemId());
            }
        } else {
            Optional<Settlement> existing = settlementRepository.findByOrderId(settlement.getOrderId());
            if (existing.isPresent()) {
                throw new SettlementException("Settlement already exists for order " + settlement.getOrderId());
            }
        }

        // Validate order eligibility
        Order order = orderRepository.findById(settlement.getOrderId())
                .orElseThrow(() -> new OrderException("Order not found: " + settlement.getOrderId()));

        if (order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new SettlementException("Order payment not completed; settlement cannot be created.");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new SettlementException("Cancelled orders cannot be settled.");
        }

        // Verify seller relationship
        if (!order.getSellerId().equals(settlement.getSellerId())) {
            throw new SettlementException("Seller does not own this order.");
        }

        // Locate order item (for per-item settlement)
        Integer itemSelling = null;
        Integer itemMrp = null;
        Integer itemQty = null;
        if (settlement.getOrderItemId() != null) {
            var match = order.getOrderItems().stream()
                    .filter(oi -> oi.getId().equals(settlement.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new SettlementException("Order item " + settlement.getOrderItemId() + " not part of order " + order.getId()));
            itemSelling = match.getSellingPrice();
            itemMrp = match.getMrpPrice();
            itemQty = match.getQuantity();
        }

        // Calculate amounts if not provided
        long gross = settlement.getGrossAmount() != null
                ? settlement.getGrossAmount()
                : itemSelling != null ? (long) itemSelling
                : order.getTotalSellingPrice() != null ? order.getTotalSellingPrice().longValue() : 0L;

        long commission = settlement.getCommissionAmount() != null
                ? settlement.getCommissionAmount()
                : Math.round(gross * DEFAULT_COMMISSION_RATE);

        long platform = settlement.getPlatformFee() != null
                ? settlement.getPlatformFee()
                : Math.round(gross * DEFAULT_PLATFORM_FEE_RATE);

        long tax = settlement.getTaxAmount() != null
                ? settlement.getTaxAmount()
                : Math.round((commission + platform) * DEFAULT_TAX_RATE);

        long net = settlement.getNetSettlementAmount() != null
                ? settlement.getNetSettlementAmount()
                : gross - commission - platform - tax;

        settlement.setGrossAmount(gross);
        settlement.setCommissionAmount(commission);
        settlement.setPlatformFee(platform);
        settlement.setTaxAmount(tax);
        settlement.setNetSettlementAmount(net);
        // Newly created settlements start as ELIGIBLE for payout.
        settlement.setSettlementStatus(SettlementStatus.ELIGIBLE);

        Settlement saved = settlementRepository.save(settlement);
        return toResponse(saved);
    }

    @Override
    public SettlementResponse getSettlement(Long id) throws SettlementException {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new SettlementException("Settlement not found with id " + id));
        return toResponse(settlement);
    }

    @Override
    public List<SettlementResponse> getSettlements(Long sellerId, SettlementStatus status, LocalDateTime from, LocalDateTime to) {
        return settlementRepository.findAllFiltered(sellerId, status, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SettlementResponse updateStatus(Long id, SettlementStatus status, String remarks) throws SettlementException {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new SettlementException("Settlement not found with id " + id));
        settlement.setSettlementStatus(status);
        if (remarks != null) {
            settlement.setRemarks(remarks);
        }
        Settlement saved = settlementRepository.save(settlement);
        return toResponse(saved);
    }

    @Override
    public SettlementSummaryResponse getSummary(Long sellerId, SettlementStatus status, LocalDateTime from, LocalDateTime to) {
        List<Settlement> items = settlementRepository.findAllFiltered(sellerId, status, from, to);

        long totalGross = items.stream().mapToLong(s -> s.getGrossAmount() == null ? 0L : s.getGrossAmount()).sum();
        long totalCommission = items.stream().mapToLong(s -> s.getCommissionAmount() == null ? 0L : s.getCommissionAmount()).sum();
        long totalPlatform = items.stream().mapToLong(s -> s.getPlatformFee() == null ? 0L : s.getPlatformFee()).sum();
        long totalTax = items.stream().mapToLong(s -> s.getTaxAmount() == null ? 0L : s.getTaxAmount()).sum();
        long totalNet = items.stream().mapToLong(s -> s.getNetSettlementAmount() == null ? 0L : s.getNetSettlementAmount()).sum();

        return SettlementSummaryResponse.builder()
                .totalGrossAmount(totalGross)
                .totalCommission(totalCommission)
                .totalPlatformFee(totalPlatform)
                .totalTax(totalTax)
                .totalNetAmount(totalNet)
                .pendingCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.PENDING).count())
                .processingCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.PROCESSING).count())
                .eligibleCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.ELIGIBLE).count())
                .onHoldCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.ON_HOLD).count())
                .completedCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.COMPLETED).count())
                .failedCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.FAILED).count())
                .cancelledCount(items.stream().filter(s -> s.getSettlementStatus() == SettlementStatus.CANCELLED).count())
                .build();
    }

    @Override
    public Settlement toEntity(SettlementRequest request) throws SellerException {
        if (request.getSellerId() == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("sellerId and orderId are required");
        }
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new SellerException("Seller not found: " + request.getSellerId()));

        Settlement settlement = new Settlement();
        settlement.setSellerId(seller.getId());
        settlement.setOrderId(request.getOrderId());
        settlement.setOrderItemId(request.getOrderItemId());
        settlement.setTransactionId(request.getTransactionId());
        settlement.setOrderReference(request.getOrderReference());
        settlement.setGrossAmount(request.getGrossAmount());
        settlement.setCommissionAmount(request.getCommissionAmount());
        settlement.setPlatformFee(request.getPlatformFee());
        settlement.setTaxAmount(request.getTaxAmount());
        settlement.setNetSettlementAmount(request.getNetSettlementAmount());
        settlement.setPaymentMethod(request.getPaymentMethod());
        settlement.setRemarks(request.getRemarks());

        if (request.getSettlementDate() != null) {
            try {
                settlement.setSettlementDate(LocalDateTime.parse(request.getSettlementDate()));
            } catch (DateTimeParseException ignored) {
                // keep null, will default in @PrePersist
            }
        }
        settlement.setSettlementStatus(SettlementStatus.PENDING);
        return settlement;
    }

    private SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .sellerId(settlement.getSellerId())
                .orderId(settlement.getOrderId())
                .orderItemId(settlement.getOrderItemId())
                .transactionId(settlement.getTransactionId())
                .orderReference(settlement.getOrderReference())
                .grossAmount(settlement.getGrossAmount())
                .commissionAmount(settlement.getCommissionAmount())
                .platformFee(settlement.getPlatformFee())
                .taxAmount(settlement.getTaxAmount())
                .netSettlementAmount(settlement.getNetSettlementAmount())
                .settlementStatus(settlement.getSettlementStatus())
                .paymentMethod(settlement.getPaymentMethod())
                .settlementDate(settlement.getSettlementDate())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .remarks(settlement.getRemarks())
                .build();
    }
}
