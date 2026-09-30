package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    User save(User user);
}
