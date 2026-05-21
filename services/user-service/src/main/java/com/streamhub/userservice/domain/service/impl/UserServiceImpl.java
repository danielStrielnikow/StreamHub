package com.streamhub.userservice.domain.service.impl;

import com.streamhub.userservice.api.exception.ConflictException;
import com.streamhub.userservice.api.exception.ResourceNotFoundException;
import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;
import com.streamhub.userservice.application.mapper.UserMapper;
import com.streamhub.userservice.domain.model.User;
import com.streamhub.userservice.domain.repository.UserRepository;
import com.streamhub.userservice.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findUserByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        User entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(entity);

        return userMapper.toResponse(savedUser);
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
