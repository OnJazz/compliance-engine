package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.dto.UserResponse;
import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            Authentication authentication
    ) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                UserResponse.from(user)
        );
    }
}