package com.ajay.ai.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ajay.payload.response.ApiResponse;

@RestController
@RequestMapping("/ai")
public class AiHomeController {

    @GetMapping
    public ResponseEntity<ApiResponse> aiHome() {
        ApiResponse response = new ApiResponse();
        response.setMessage("Welcome to AI World!");
        response.setStatus(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}