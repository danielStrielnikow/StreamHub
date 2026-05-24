package com.streamhub.authservice.domain.service.impl;

import com.streamhub.authservice.domain.service.AuthService;
import com.streamhub.authservice.dto.reponse.TokenResponse;
import com.streamhub.authservice.dto.request.LoginRequest;
import com.streamhub.authservice.dto.request.RefreshTokenRequest;
import com.streamhub.authservice.dto.request.RegisterRequest;
import com.streamhub.authservice.infrastructure.keylock.KeycloakService;
import com.streamhub.authservice.infrastructure.messaging.UserEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final KeycloakService keycloakService;
    private final UserEventProducer userEventProducer;
    
    @Override
    public TokenResponse register(RegisterRequest request) {
        UUID userId = keycloakService.registerUser(request);
        userEventProducer.publish(userId, request.email());
        return keycloakService.getToken(request.email(), request.password());
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        return keycloakService.getToken(request.email(), request.password());
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        return keycloakService.refreshToken(request.refreshToken());
    }
}
