package com.streamhub.userservice.application.dto.response;

import com.streamhub.userservice.domain.model.UserType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        UserType userType,
        LocalDateTime createdAt
) {
}
