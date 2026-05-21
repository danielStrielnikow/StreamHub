package com.streamhub.userservice.application.dto.request;

import com.streamhub.userservice.application.validator.ValidPassword;
import com.streamhub.userservice.domain.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @Email @NotBlank String email,
        @ValidPassword String password,
        @NotNull UserType userType
) {
}
