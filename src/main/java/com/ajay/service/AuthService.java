package com.ajay.service;

import com.ajay.exception.SellerException;
import com.ajay.exception.UserException;
import com.ajay.request.LoginRequest;
import com.ajay.request.ResetPasswordRequest;
import com.ajay.request.SignupRequest;
import com.ajay.response.ApiResponse;
import com.ajay.response.AuthResponse;

import jakarta.mail.MessagingException;

public interface AuthService {
    void sentLoginOtp(String email) throws UserException, MessagingException;
    String createUser(SignupRequest req) throws SellerException;
    AuthResponse signin(LoginRequest req) throws SellerException;
}
