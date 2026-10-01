package com.jasonvennin.compliance.api.dto;

import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;

import java.util.Set;

public record UserResponse(
        String id,
        String username,
        Set<Role> roles,
        boolean enabled
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.roles(),
                user.enabled()
        );
    }
}