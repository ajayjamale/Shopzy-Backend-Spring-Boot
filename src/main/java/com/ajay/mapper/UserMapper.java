package com.ajay.mapper;

import com.ajay.dto.OrderDto;
import com.ajay.dto.OrderItemDto;
import com.ajay.dto.UserDto;
import com.ajay.model.Order;
import com.ajay.model.OrderItem;
import com.ajay.model.User;

public class UserMapper {

    public static UserDto toUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFullName(user.getFullName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

}
