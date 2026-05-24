package com.streamhub.authservice.domain.service;

import com.streamhub.authservice.dto.reponse.TokenResponse;
import com.streamhub.authservice.dto.request.LoginRequest;
import com.streamhub.authservice.dto.request.RefreshTokenRequest;
import com.streamhub.authservice.dto.request.RegisterRequest;

public interface AuthService {
    TokenResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
}
