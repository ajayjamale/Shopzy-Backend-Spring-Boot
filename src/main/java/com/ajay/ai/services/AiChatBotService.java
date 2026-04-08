package com.ajay.ai.services;

import com.ajay.exception.ProductException;
import com.ajay.payload.response.ApiResponse;

public interface AiChatBotService {

    ApiResponse aiChatBot(String prompt, Long productId, Long userId, Long sellerId, String mode) throws ProductException;
}
