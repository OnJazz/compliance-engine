package com.jasonvennin.compliance.infrastructure.security;

import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.user.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MongoUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.username())
                .password(user.passwordHash())
                .roles(
                        user.roles()
                                .stream()
                                .map(Enum::name)
                                .toArray(String[]::new)
                )
                .disabled(!user.enabled())
                .build();
    }
}