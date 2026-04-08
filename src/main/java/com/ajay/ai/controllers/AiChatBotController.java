package com.ajay.ai.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ajay.ai.services.AiChatBotService;
import com.ajay.model.Seller;
import com.ajay.model.User;
import com.ajay.payload.request.Prompt;
import com.ajay.payload.response.ApiResponse;
import com.ajay.service.SellerService;
import com.ajay.service.UserService;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/chat")
public class AiChatBotController {

    private final AiChatBotService aiChatBotService;
    private final UserService userService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ApiResponse> generate(
            @Valid @RequestBody Prompt prompt,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false, defaultValue = "customer") String mode,
            @RequestHeader(required = false, name = "Authorization") String jwt) throws Exception {

        // Build the message, optionally prepending product context
        String message = prompt.getPrompt();
        if (productId != null) {
            message = "The product id is " + productId + ". " + message;
        }

        Long userId = null;
        Long sellerId = null;
        if (jwt != null && !jwt.isBlank()) {
            String normalizedMode = mode == null ? "customer" : mode.trim().toLowerCase();

            if ("seller".equals(normalizedMode)) {
                sellerId = resolveSellerId(jwt);
                if (sellerId == null) {
                    userId = resolveUserId(jwt);
                }
            } else {
                userId = resolveUserId(jwt);
                if (userId == null) {
                    sellerId = resolveSellerId(jwt);
                }
            }
        }

        ApiResponse apiResponse = aiChatBotService.aiChatBot(message, productId, userId, sellerId, mode);
        return ResponseEntity.ok(apiResponse);
    }

    private Long resolveUserId(String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            return user != null ? user.getId() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long resolveSellerId(String jwt) {
        try {
            Seller seller = sellerService.getSellerProfile(jwt);
            return seller != null ? seller.getId() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
