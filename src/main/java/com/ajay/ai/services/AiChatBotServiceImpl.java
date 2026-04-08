package com.ajay.ai.services;

import com.ajay.model.Cart;
import com.ajay.model.CartItem;
import com.ajay.model.Order;
import com.ajay.model.OrderItem;
import com.ajay.model.Product;
import com.ajay.model.ReturnRequest;
import com.ajay.model.Seller;
import com.ajay.model.SellerReport;
import com.ajay.model.Settlement;
import com.ajay.model.Transaction;
import com.ajay.model.User;
import com.ajay.domains.OrderStatus;
import com.ajay.domains.PaymentStatus;
import com.ajay.domains.ReturnStatus;
import com.ajay.domains.SettlementStatus;
import com.ajay.exception.ProductException;
import com.ajay.payload.response.ApiResponse;
import com.ajay.repository.CartRepository;
import com.ajay.repository.OrderRepository;
import com.ajay.repository.ProductRepository;
import com.ajay.repository.ReturnRequestRepository;
import com.ajay.repository.SellerReportRepository;
import com.ajay.repository.SellerRepository;
import com.ajay.repository.SettlementRepository;
import com.ajay.repository.TransactionRepository;
import com.ajay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatBotServiceImpl implements AiChatBotService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final SettlementRepository settlementRepository;
    private final SellerReportRepository sellerReportRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public ApiResponse aiChatBot(String prompt, Long productId, Long userId, Long sellerId, String mode) throws ProductException {
        String cleanPrompt = prompt == null ? "" : prompt.trim();
        JSONObject dbContext = buildDbContext(cleanPrompt, productId, userId, sellerId, mode);

        String deterministicReply = buildDeterministicReply(cleanPrompt, dbContext);
        if (deterministicReply != null) {
            return new ApiResponse(deterministicReply, true);
        }

        String reply;
        if (isGroqConfigured()) {
            try {
                reply = callGroq(dbContext.toString(), cleanPrompt);
            } catch (Exception ex) {
                reply = buildFallbackReply(dbContext, cleanPrompt);
            }
        } else {
            reply = buildFallbackReply(dbContext, cleanPrompt);
        }

        return new ApiResponse(reply, true);
    }

    private JSONObject buildDbContext(String prompt, Long productId, Long userId, Long sellerId, String mode) throws ProductException {
        JSONObject context = new JSONObject();
        String scope = resolveScope(mode, userId, sellerId);
        context.put("scope", scope);
        context.put("generatedAt", LocalDateTime.now().toString());

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductException("Product not found: " + productId));
            context.put("product", mapProduct(product));
        }

        if ("SELLER".equals(scope)) {
            buildSellerContext(context, sellerId);
            return context;
        }

        if ("CUSTOMER".equals(scope)) {
            buildCustomerContext(context, userId);
            return context;
        }

        context.put("authenticated", false);
        context.put("note", "Guest mode. Sign in for personalized order/cart/seller data.");
        return context;
    }

    private String resolveScope(String mode, Long userId, Long sellerId) {
        String normalizedMode = mode == null ? "" : mode.trim().toLowerCase(Locale.ROOT);

        if (sellerId != null && userId == null) return "SELLER";
        if (userId != null && sellerId == null) return "CUSTOMER";
        if ("seller".equals(normalizedMode)) return "SELLER";
        if ("customer".equals(normalizedMode)) return "CUSTOMER";
        if (sellerId != null) return "SELLER";
        if (userId != null) return "CUSTOMER";
        return "GUEST";
    }

    private void buildCustomerContext(JSONObject context, Long userId) {
        if (userId == null) {
            context.put("authenticated", false);
            context.put("note", "Customer is not logged in.");
            return;
        }

        context.put("authenticated", true);
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            context.put("userProfile", new JSONObject()
                    .put("id", user.getId())
                    .put("fullName", valueOrEmpty(user.getFullName()))
                    .put("email", valueOrEmpty(user.getEmail()))
                    .put("mobile", valueOrEmpty(user.getMobile())));
        }

        Cart cart = cartRepository.findByUserId(userId);
        context.put("cart", summarizeCart(cart));

        List<Order> paidOrders = orderRepository.findByUserIdAndPaymentStatusOrderByOrderDateDesc(userId, PaymentStatus.COMPLETED);
        context.put("orderStats", summarizeCustomerOrderStats(paidOrders));
        context.put("recentOrders", mapOrders(paidOrders, 50));

        List<ReturnRequest> returns = returnRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        context.put("returnStats", summarizeReturns(returns));
    }

    private void buildSellerContext(JSONObject context, Long sellerId) {
        if (sellerId == null) {
            context.put("authenticated", false);
            context.put("note", "Seller is not logged in.");
            return;
        }

        context.put("authenticated", true);
        Seller seller = sellerRepository.findById(sellerId).orElse(null);
        if (seller != null) {
            context.put("sellerProfile", new JSONObject()
                    .put("id", seller.getId())
                    .put("sellerName", valueOrEmpty(seller.getSellerName()))
                    .put("email", valueOrEmpty(seller.getEmail()))
                    .put("mobile", valueOrEmpty(seller.getMobile())));
        }

        List<Product> products = productRepository.findBySellerId(sellerId);
        context.put("productStats", summarizeSellerProducts(products));

        List<Order> paidOrders = orderRepository.findBySellerIdAndPaymentStatusOrderByOrderDateDesc(sellerId, PaymentStatus.COMPLETED);
        context.put("orderStats", summarizeSellerOrderStats(paidOrders));
        context.put("recentOrders", mapOrders(paidOrders, 80));

        List<ReturnRequest> returns = returnRequestRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        context.put("returnStats", summarizeReturns(returns));

        List<Settlement> settlements = settlementRepository.findBySellerId(sellerId);
        context.put("settlementStats", summarizeSettlements(settlements));

        List<Transaction> transactions = transactionRepository.findBySellerId(sellerId);
        context.put("transactionStats", summarizeTransactions(transactions));

        SellerReport report = sellerReportRepository.findBySellerId(sellerId);
        if (report != null) {
            context.put("sellerReport", new JSONObject()
                    .put("totalEarnings", nullSafeLong(report.getTotalEarnings()))
                    .put("totalSales", nullSafeLong(report.getTotalSales()))
                    .put("totalRefunds", nullSafeLong(report.getTotalRefunds()))
                    .put("totalTax", nullSafeLong(report.getTotalTax()))
                    .put("netEarnings", nullSafeLong(report.getNetEarnings()))
                    .put("totalOrders", nullSafeInt(report.getTotalOrders()))
                    .put("canceledOrders", nullSafeInt(report.getCanceledOrders()))
                    .put("totalTransactions", nullSafeInt(report.getTotalTransactions())));
        }
    }

    private JSONObject summarizeCart(Cart cart) {
        JSONObject summary = new JSONObject();
        if (cart == null) {
            return summary
                    .put("exists", false)
                    .put("totalItems", 0)
                    .put("distinctProducts", 0)
                    .put("totalSellingPrice", 0)
                    .put("items", new JSONArray());
        }

        List<CartItem> items = cart.getCartItems() == null
                ? Collections.emptyList()
                : cart.getCartItems().stream().toList();
        int totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();

        JSONArray itemArray = new JSONArray();
        for (CartItem item : items) {
            if (item == null) continue;
            itemArray.put(new JSONObject()
                    .put("cartItemId", item.getId())
                    .put("productId", item.getProduct() != null ? item.getProduct().getId() : JSONObject.NULL)
                    .put("title", item.getProduct() != null ? valueOrEmpty(item.getProduct().getTitle()) : "")
                    .put("quantity", item.getQuantity())
                    .put("size", valueOrEmpty(item.getSize()))
                    .put("sellingPrice", nullSafeInt(item.getSellingPrice())));
        }

        return summary
                .put("exists", true)
                .put("totalItems", totalItems)
                .put("distinctProducts", items.size())
                .put("totalSellingPrice", nullSafeDouble(cart.getTotalSellingPrice()))
                .put("items", itemArray);
    }

    private JSONObject summarizeCustomerOrderStats(List<Order> orders) {
        int totalOrders = orders == null ? 0 : orders.size();
        int totalItemsOrdered = orders == null ? 0 : orders.stream().mapToInt(this::sumOrderItemQuantity).sum();
        long deliveredOrders = orders == null ? 0 : orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = orders == null ? 0 : orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
        long activeOrders = totalOrders - deliveredOrders - cancelledOrders;
        long totalSpent = orders == null ? 0 : orders.stream().mapToLong(o -> nullSafeInt(o.getTotalSellingPrice())).sum();
        long distinctProducts = orders == null ? 0 : orders.stream()
                .flatMap(o -> safeOrderItems(o).stream())
                .map(OrderItem::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .size();

        return new JSONObject()
                .put("totalOrders", totalOrders)
                .put("totalItemsOrdered", totalItemsOrdered)
                .put("distinctProductsOrdered", distinctProducts)
                .put("deliveredOrders", deliveredOrders)
                .put("cancelledOrders", cancelledOrders)
                .put("activeOrders", activeOrders)
                .put("totalSpent", totalSpent);
    }

    private JSONObject summarizeSellerOrderStats(List<Order> orders) {
        int totalOrders = orders == null ? 0 : orders.size();
        int totalItemsSold = orders == null ? 0 : orders.stream().mapToInt(this::sumOrderItemQuantity).sum();
        long deliveredOrders = orders == null ? 0 : orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = orders == null ? 0 : orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
        long openOrders = totalOrders - deliveredOrders - cancelledOrders;
        long grossSales = orders == null ? 0 : orders.stream().mapToLong(o -> nullSafeInt(o.getTotalSellingPrice())).sum();

        return new JSONObject()
                .put("totalOrders", totalOrders)
                .put("totalItemsSold", totalItemsSold)
                .put("deliveredOrders", deliveredOrders)
                .put("cancelledOrders", cancelledOrders)
                .put("openOrders", openOrders)
                .put("grossSales", grossSales);
    }

    private JSONObject summarizeSellerProducts(List<Product> products) {
        int totalProducts = products == null ? 0 : products.size();
        int outOfStock = products == null ? 0 : (int) products.stream().filter(p -> p.getQuantity() <= 0).count();
        int lowStock = products == null ? 0 : (int) products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() <= 5).count();

        JSONArray topLowStock = new JSONArray();
        if (products != null) {
            products.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getQuantity() <= 5)
                    .sorted((a, b) -> Integer.compare(a.getQuantity(), b.getQuantity()))
                    .limit(20)
                    .forEach(p -> topLowStock.put(new JSONObject()
                            .put("productId", p.getId())
                            .put("title", valueOrEmpty(p.getTitle()))
                            .put("quantity", p.getQuantity())));
        }

        return new JSONObject()
                .put("totalProducts", totalProducts)
                .put("inStockProducts", Math.max(totalProducts - outOfStock, 0))
                .put("outOfStockProducts", outOfStock)
                .put("lowStockProducts", lowStock)
                .put("lowStockItems", topLowStock);
    }

    private JSONObject summarizeReturns(List<ReturnRequest> returns) {
        int totalReturns = returns == null ? 0 : returns.size();
        int openReturns = returns == null ? 0 : (int) returns.stream()
                .filter(r -> isOpenReturnStatus(r.getStatus()))
                .count();

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        if (returns != null) {
            for (ReturnRequest request : returns) {
                String key = request.getStatus() == null ? "UNKNOWN" : request.getStatus().name();
                statusCounts.put(key, statusCounts.getOrDefault(key, 0) + 1);
            }
        }

        return new JSONObject()
                .put("totalReturns", totalReturns)
                .put("openReturns", openReturns)
                .put("statusCounts", new JSONObject(statusCounts));
    }

    private JSONObject summarizeSettlements(List<Settlement> settlements) {
        int totalSettlements = settlements == null ? 0 : settlements.size();
        long totalNetAmount = settlements == null ? 0 : settlements.stream()
                .mapToLong(s -> nullSafeLong(s.getNetSettlementAmount()))
                .sum();
        int pendingSettlements = settlements == null ? 0 : (int) settlements.stream()
                .filter(s -> isPendingSettlementStatus(s.getSettlementStatus()))
                .count();

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        if (settlements != null) {
            for (Settlement settlement : settlements) {
                String key = settlement.getSettlementStatus() == null ? "UNKNOWN" : settlement.getSettlementStatus().name();
                statusCounts.put(key, statusCounts.getOrDefault(key, 0) + 1);
            }
        }

        return new JSONObject()
                .put("totalSettlements", totalSettlements)
                .put("pendingSettlements", pendingSettlements)
                .put("totalNetSettlementAmount", totalNetAmount)
                .put("statusCounts", new JSONObject(statusCounts));
    }

    private JSONObject summarizeTransactions(List<Transaction> transactions) {
        int totalTransactions = transactions == null ? 0 : transactions.size();
        String latestDate = transactions == null ? "" : transactions.stream()
                .map(Transaction::getDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElse("");

        return new JSONObject()
                .put("totalTransactions", totalTransactions)
                .put("latestTransactionAt", latestDate);
    }

    private JSONArray mapOrders(List<Order> orders, int limit) {
        JSONArray array = new JSONArray();
        if (orders == null) return array;

        int count = 0;
        for (Order order : orders) {
            if (order == null) continue;
            array.put(mapOrder(order));
            count++;
            if (count >= limit) break;
        }
        return array;
    }

    private JSONObject mapOrder(Order order) {
        JSONArray items = new JSONArray();
        for (OrderItem item : safeOrderItems(order)) {
            items.put(mapOrderItem(item));
        }

        return new JSONObject()
                .put("id", order.getId())
                .put("orderId", valueOrEmpty(order.getOrderId()))
                .put("orderStatus", order.getOrderStatus() != null ? order.getOrderStatus().name() : "UNKNOWN")
                .put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "UNKNOWN")
                .put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : "")
                .put("totalItem", order.getTotalItem())
                .put("totalSellingPrice", nullSafeInt(order.getTotalSellingPrice()))
                .put("items", items);
    }

    private JSONObject mapOrderItem(OrderItem item) {
        Product product = item.getProduct();
        return new JSONObject()
                .put("orderItemId", item.getId())
                .put("productId", product != null ? product.getId() : JSONObject.NULL)
                .put("title", product != null ? valueOrEmpty(product.getTitle()) : "")
                .put("quantity", item.getQuantity())
                .put("size", valueOrEmpty(item.getSize()))
                .put("sellingPrice", nullSafeInt(item.getSellingPrice()));
    }

    private JSONObject mapProduct(Product product) {
        return new JSONObject()
                .put("id", product.getId())
                .put("title", valueOrEmpty(product.getTitle()))
                .put("description", valueOrEmpty(product.getDescription()))
                .put("category", product.getCategory() != null ? valueOrEmpty(product.getCategory().getName()) : "")
                .put("sellingPrice", product.getSellingPrice())
                .put("mrpPrice", product.getMrpPrice())
                .put("discountPercent", product.getDiscountPercent())
                .put("stockQuantity", product.getQuantity())
                .put("sellerId", product.getSeller() != null ? product.getSeller().getId() : JSONObject.NULL);
    }

    private int sumOrderItemQuantity(Order order) {
        return safeOrderItems(order).stream().mapToInt(OrderItem::getQuantity).sum();
    }

    private List<OrderItem> safeOrderItems(Order order) {
        if (order == null || order.getOrderItems() == null) return Collections.emptyList();
        return order.getOrderItems();
    }

    private boolean isOpenReturnStatus(ReturnStatus status) {
        return status == ReturnStatus.REQUESTED
                || status == ReturnStatus.APPROVED
                || status == ReturnStatus.PICKUP_SCHEDULED
                || status == ReturnStatus.RECEIVED
                || status == ReturnStatus.REFUND_INITIATED;
    }

    private boolean isPendingSettlementStatus(SettlementStatus status) {
        return status == SettlementStatus.PENDING
                || status == SettlementStatus.PROCESSING
                || status == SettlementStatus.ELIGIBLE
                || status == SettlementStatus.ON_HOLD;
    }

    private String buildDeterministicReply(String prompt, JSONObject context) {
        String lower = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        boolean asksHowMany = containsAny(lower, "how many", "count", "number of", "total");
        boolean asksOrder = lower.contains("order");
        boolean asksItem = containsAny(lower, "item", "items");
        boolean asksCart = containsAny(lower, "cart", "bag", "basket");
        boolean asksReturn = containsAny(lower, "return", "refund");
        boolean asksProduct = containsAny(lower, "product", "listing", "inventory", "stock");
        boolean asksSettlement = containsAny(lower, "settlement", "payout", "payment");

        String scope = context.optString("scope", "GUEST");
        boolean authenticated = context.optBoolean("authenticated", false);

        if ("CUSTOMER".equals(scope) && !authenticated && (asksOrder || asksCart || asksReturn)) {
            return "Please sign in to view your cart, orders, and return data.";
        }
        if ("SELLER".equals(scope) && !authenticated && (asksOrder || asksProduct || asksReturn || asksSettlement)) {
            return "Please sign in with your seller account to access seller chatbot data.";
        }

        if ("CUSTOMER".equals(scope)) {
            JSONObject orderStats = context.optJSONObject("orderStats");
            JSONObject cart = context.optJSONObject("cart");
            JSONObject returnStats = context.optJSONObject("returnStats");

            if (asksHowMany && asksOrder && asksItem && orderStats != null) {
                int totalItems = orderStats.optInt("totalItemsOrdered", 0);
                int totalOrders = orderStats.optInt("totalOrders", 0);
                return "You have ordered " + totalItems + " items across " + totalOrders + " paid orders.";
            }
            if (asksHowMany && asksOrder && orderStats != null) {
                int totalOrders = orderStats.optInt("totalOrders", 0);
                return "You have " + totalOrders + " paid orders.";
            }
            if (asksHowMany && asksCart && asksItem && cart != null) {
                int totalItems = cart.optInt("totalItems", 0);
                return "Your cart currently has " + totalItems + " item" + (totalItems == 1 ? "" : "s") + ".";
            }
            if (asksHowMany && asksReturn && returnStats != null) {
                int totalReturns = returnStats.optInt("totalReturns", 0);
                return "You have " + totalReturns + " return request" + (totalReturns == 1 ? "" : "s") + ".";
            }
        }

        if ("SELLER".equals(scope)) {
            JSONObject orderStats = context.optJSONObject("orderStats");
            JSONObject productStats = context.optJSONObject("productStats");
            JSONObject returnStats = context.optJSONObject("returnStats");
            JSONObject settlementStats = context.optJSONObject("settlementStats");

            if (asksHowMany && asksOrder && asksItem && orderStats != null) {
                int totalItemsSold = orderStats.optInt("totalItemsSold", 0);
                int totalOrders = orderStats.optInt("totalOrders", 0);
                return "You have sold " + totalItemsSold + " items across " + totalOrders + " paid orders.";
            }
            if (asksHowMany && asksOrder && orderStats != null) {
                int totalOrders = orderStats.optInt("totalOrders", 0);
                return "You have " + totalOrders + " paid seller orders.";
            }
            if (asksHowMany && asksProduct && productStats != null) {
                int totalProducts = productStats.optInt("totalProducts", 0);
                int outOfStock = productStats.optInt("outOfStockProducts", 0);
                return "You have " + totalProducts + " products, with " + outOfStock + " currently out of stock.";
            }
            if (asksHowMany && asksReturn && returnStats != null) {
                int openReturns = returnStats.optInt("openReturns", 0);
                int totalReturns = returnStats.optInt("totalReturns", 0);
                return "You have " + openReturns + " open returns out of " + totalReturns + " total return requests.";
            }
            if (asksHowMany && asksSettlement && settlementStats != null) {
                int pendingSettlements = settlementStats.optInt("pendingSettlements", 0);
                int totalSettlements = settlementStats.optInt("totalSettlements", 0);
                return "You have " + pendingSettlements + " pending settlements out of " + totalSettlements + " total settlements.";
            }
        }

        return null;
    }

    private boolean containsAny(String input, String... candidates) {
        for (String candidate : candidates) {
            if (input.contains(candidate)) return true;
        }
        return false;
    }

    private String callGroq(String contextData, String userPrompt) {
        JSONObject systemMessage = new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are Shopzy's data assistant. Use only facts from DB_CONTEXT_JSON. "
                                + "Never invent numbers or products. "
                                + "If data is missing, clearly say you could not find it in database data. "
                                + "Reply in 1-3 concise sentences and plain text.");

        JSONObject userMessage = new JSONObject()
                .put("role", "user")
                .put("content",
                        "USER_QUESTION:\n" + userPrompt + "\n\nDB_CONTEXT_JSON:\n" + contextData);

        JSONObject requestBody = new JSONObject()
                .put("model", GROQ_MODEL)
                .put("messages", new JSONArray().put(systemMessage).put(userMessage))
                .put("max_tokens", 220)
                .put("temperature", 0.1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, request, String.class);

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("Empty response body from Groq");
        }

        JSONObject root = new JSONObject(body);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IllegalStateException("Groq response does not contain choices");
        }

        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        String content = message != null ? message.optString("content", "").trim() : "";
        if (content.isBlank()) {
            throw new IllegalStateException("Groq response does not contain message content");
        }
        return content;
    }

    private boolean isGroqConfigured() {
        if (groqApiKey == null) return false;
        String key = groqApiKey.trim();
        if (key.isBlank()) return false;
        return !key.startsWith("AIza");
    }

    private String buildFallbackReply(JSONObject context, String prompt) {
        String deterministic = buildDeterministicReply(prompt, context);
        if (deterministic != null) return deterministic;

        String scope = context.optString("scope", "GUEST");
        boolean authenticated = context.optBoolean("authenticated", false);

        if ("SELLER".equals(scope) && !authenticated) {
            return "Please sign in with your seller account to access seller orders, products, settlements, and return data.";
        }
        if ("CUSTOMER".equals(scope) && !authenticated) {
            return "Please sign in to access your cart, orders, and return history.";
        }

        return "The live AI model is unavailable right now. You can still ask for exact counts like total orders, items, returns, products, or settlements.";
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private int nullSafeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double nullSafeDouble(double value) {
        return value;
    }
}
