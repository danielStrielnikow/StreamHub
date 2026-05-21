package com.streamhub.userservice.application.mapper;

import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;
import com.streamhub.userservice.domain.model.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);
}
