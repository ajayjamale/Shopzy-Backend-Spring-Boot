package com.ajay.ai.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ajay.ai.services.AiChatBotService;
import com.ajay.model.User;
import com.ajay.request.Prompt;
import com.ajay.response.ApiResponse;
import com.ajay.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/chat")
public class AiChatBotController {

    private final AiChatBotService aiChatBotService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> generate(
            @RequestBody Prompt prompt,
            @RequestParam(required = false) Long productId,
            @RequestHeader(required = false, name = "Authorization") String jwt) throws Exception {

        // Build the message, optionally prepending product context
        String message = prompt.getPrompt();
        if (productId != null) {
            message = "The product id is " + productId + ". " + message;
        }

        // Resolve user from JWT if provided, otherwise use a guest user
        Long userId = null;
        if (jwt != null && !jwt.isBlank()) {
            User user = userService.findUserProfileByJwt(jwt);
            if (user != null) {
                userId = user.getId();
            }
        }

        ApiResponse apiResponse = aiChatBotService.aiChatBot(message, productId, userId);
        return ResponseEntity.ok(apiResponse);
    }
}