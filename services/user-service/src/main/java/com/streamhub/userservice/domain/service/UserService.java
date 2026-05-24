package com.streamhub.userservice.domain.service;

import com.streamhub.common.event.UserRegisteredEvent;
import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    void createFromEvent(UserRegisteredEvent event);

    UserResponse getUserById(UUID userId);
    
    List<UserResponse> getAllUser();

    UserResponse updateUser(UUID userId, UserRequest request);

    void deleteUser(UUID userId);
    
}
