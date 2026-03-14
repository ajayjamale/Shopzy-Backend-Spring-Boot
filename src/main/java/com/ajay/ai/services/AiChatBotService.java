package com.ajay.ai.services;

import com.ajay.exception.ProductException;
import com.ajay.response.ApiResponse;

public interface AiChatBotService {

    ApiResponse aiChatBot(String prompt,Long productId,Long userId) throws ProductException;
}
