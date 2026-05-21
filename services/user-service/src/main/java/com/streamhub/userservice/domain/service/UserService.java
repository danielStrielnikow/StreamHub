package com.streamhub.userservice.domain.service;

import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(UUID userId);
    
    List<UserResponse> getAllUser();

    UserResponse updateUser(UUID userId, UserRequest request);

    void deleteUser(UUID userId);
    
}
