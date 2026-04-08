package com.ajay.service;

import com.ajay.exception.SellerException;
import com.ajay.exception.UserException;
import com.ajay.payload.request.LoginRequest;
import com.ajay.payload.request.ResetPasswordRequest;
import com.ajay.payload.request.SignupRequest;
import com.ajay.payload.response.ApiResponse;
import com.ajay.payload.response.AuthResponse;

import jakarta.mail.MessagingException;

public interface AuthService {
    void sentLoginOtp(String email) throws UserException, MessagingException;
    String createUser(SignupRequest req) throws SellerException;
    AuthResponse signin(LoginRequest req) throws SellerException;
}
