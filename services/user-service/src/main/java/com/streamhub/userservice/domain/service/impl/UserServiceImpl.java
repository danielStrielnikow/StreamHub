package com.streamhub.userservice.domain.service.impl;

import com.streamhub.common.event.UserRegisteredEvent;
import com.streamhub.userservice.api.exception.ResourceNotFoundException;
import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;
import com.streamhub.userservice.application.mapper.UserMapper;
import com.streamhub.userservice.domain.model.User;
import com.streamhub.userservice.domain.repository.UserRepository;
import com.streamhub.userservice.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createFromEvent(UserRegisteredEvent event) {
        if (userRepository.existsByKeycloakId(event.userId())) {
            log.warn("User {} already exists, skipping", event.userId());
            return;
        }
        userRepository.save(userMapper.fromEvent(event));
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found "));
        
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> allUsers = userRepository.findAll();
        
        return userMapper.toListResponse(allUsers);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found "));
        user.setEmail(request.email());
        user.setUserType(request.userType());
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found "));
        userRepository.delete(user);
    }
}
