package com.ajay.ai.services;

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

import com.ajay.exception.ProductException;
import com.ajay.mapper.OrderMapper;
import com.ajay.mapper.ProductMapper;
import com.ajay.model.Cart;
import com.ajay.model.Order;
import com.ajay.model.Product;
import com.ajay.model.User;
import com.ajay.repository.CartRepository;
import com.ajay.repository.OrderRepository;
import com.ajay.repository.ProductRepository;
import com.ajay.repository.UserRepository;
import com.ajay.response.ApiResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatBotServiceImpl implements AiChatBotService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    // Groq API endpoint — free, fast, no model version issues
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // Best free model on Groq
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse aiChatBot(String prompt, Long productId, Long userId) throws ProductException {

        System.out.println("------- prompt: " + prompt);

        // Step 1: Fetch relevant DB data based on what user is asking
        String contextData = fetchContextData(prompt, productId, userId);
        System.out.println("------- contextData: " + contextData);

        // Step 2: Call Groq with context + user prompt
        String reply = callGroq(contextData, prompt);
        System.out.println("------- groq reply: " + reply);

        ApiResponse res = new ApiResponse();
        res.setMessage(reply);
        res.setStatus(true);
        return res;
    }

    /**
     * Keyword-based context fetcher.
     * Decides what DB data to load based on what the user is asking about.
     */
    private String fetchContextData(String prompt, Long productId, Long userId) throws ProductException {
        String lower = prompt.toLowerCase();

        // Product question
        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductException("Product not found: " + productId));
            return "PRODUCT DATA: " + ProductMapper.toProductDto(product).toString();
        }

        // Cart question
        if (lower.contains("cart") || lower.contains("basket")
                || lower.contains("bag") || lower.contains("added")) {
            if (userId == null) return "User is not logged in. No cart data available.";
            Cart cart = cartRepository.findByUserId(userId);
            if (cart == null) return "No cart found for this user.";
            return "CART DATA: Cart ID=" + cart.getId()
                    + ", Total Items=" + cart.getCartItems().size()
                    + ", Items=" + cart.getCartItems().toString();
        }

        // Order question
        if (lower.contains("order") || lower.contains("purchase")
                || lower.contains("bought") || lower.contains("delivery")
                || lower.contains("delivered") || lower.contains("cancelled")) {
            if (userId == null) return "User is not logged in. No order data available.";
            User user = userRepository.findById(userId).orElse(null);
            List<Order> orders = orderRepository.findByUserId(userId);
            if (orders == null || orders.isEmpty()) return "No orders found for this user.";
            return "ORDER DATA: " + OrderMapper.toOrderHistory(orders, user).toString();
        }

        return "General shopping assistant query. Answer helpfully about Shopzy e-commerce platform.";
    }

    /**
     * Calls Groq API using OpenAI-compatible format.
     * Returns the assistant's reply text directly.
     */
    private String callGroq(String contextData, String userPrompt) {
        // System message gives Groq the context and role
        JSONObject systemMessage = new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are a concise shopping assistant for Shopzy. Answer in 1-3 short sentences only. No bullet points. Be direct. Only use provided data. "
                        + "Relevant data:\n\n"
                        + contextData);

        // User message is the actual question
        JSONObject userMessage = new JSONObject()
                .put("role", "user")
                .put("content", userPrompt);

        JSONObject requestBody = new JSONObject()
                .put("model", GROQ_MODEL)
                .put("messages", new JSONArray()
                        .put(systemMessage)
                        .put(userMessage))
                .put("max_tokens", 120)
                .put("temperature", 0.5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey); // Groq uses Bearer token auth

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, request, String.class);

        // Parse OpenAI-compatible response format
        return new JSONObject(response.getBody())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}