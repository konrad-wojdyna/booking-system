package com.booking.bookingsystem.dto.response;

import com.booking.bookingsystem.model.Role;
import com.booking.bookingsystem.model.User;

public record UserResponse(
        String email,
        String name,
        String token,
        Role role
) {

    public static UserResponse from(User user, String token, Role role){
        return new UserResponse(
                user.getEmail(),
                user.getName(),
                token,
                role
        );
    }
}
