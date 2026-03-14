package com.ajay.ai.controllers;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ajay.ai.services.AiProductService;
import com.ajay.response.ApiResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AiProductController {

    private final AiProductService productService;

    @PostMapping("/chat/demo")
    public ResponseEntity<ApiResponse> generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) throws Exception {

        ApiResponse apiResponse = new ApiResponse();

        // Get raw response string from AI service
        String rawResponse = productService.simpleChat(message);

        // Parse the Gemini JSON response structure
        JSONObject responseJson = new JSONObject(rawResponse);
        JSONArray candidates = responseJson.getJSONArray("candidates");

        if (candidates.length() == 0) {
            apiResponse.setMessage("No response from AI.");
            apiResponse.setStatus(false);
            return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
        }

        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        String text = parts.getJSONObject(0).getString("text");

        apiResponse.setMessage(text);
        apiResponse.setStatus(true);
        return ResponseEntity.ok(apiResponse);
    }
}