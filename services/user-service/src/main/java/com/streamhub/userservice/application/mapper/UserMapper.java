package com.streamhub.userservice.application.mapper;

import com.streamhub.userservice.application.dto.request.UserRequest;
import com.streamhub.userservice.application.dto.response.UserResponse;
import com.streamhub.userservice.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface UserMapper {
    
    @Mapping(source = "id", target = "userId")
    UserResponse toResponse(User user);

    List<UserResponse> toListResponse(List<User> users);
    
    User toEntity(UserRequest request);
}
