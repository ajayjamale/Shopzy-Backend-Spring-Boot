package com.ajay.mapper;

import com.ajay.payload.response.UserResponse;
import com.ajay.model.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        return response;
    }

    public static User toEntity(UserResponse response) {
        if (response == null) {
            return null;
        }
        User user = new User();
        user.setId(response.getId());
        user.setFullName(response.getFullName());
        user.setEmail(response.getEmail());
        return user;
    }

    public static User updateEntity(User user, User sourceUser) {
        if (user == null || sourceUser == null) {
            return user;
        }
        user.setFullName(sourceUser.getFullName());
        user.setEmail(sourceUser.getEmail());
        return user;
    }

    public static UserResponse toUserResponse(User user) {
        return toResponse(user);
    }

}
