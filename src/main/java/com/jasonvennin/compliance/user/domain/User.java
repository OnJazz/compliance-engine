package com.jasonvennin.compliance.user.domain;

import java.util.Set;

public record User(
        String id,
        String username,
        String passwordHash,
        Set<Role> roles,
        boolean enabled
) {
}
