package com.jasonvennin.compliance.infrastructure.security;

import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MongoUserDetailsService userDetailsService;

    @Test
    void shouldLoadUser() {

        User user = new User(
                "user-id",
                "admin",
                "$2a$10$hashedPassword",
                Set.of(Role.ADMIN),
                true
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername())
                .isEqualTo("admin");

        assertThat(result.getPassword())
                .isEqualTo("$2a$10$hashedPassword");

        assertThat(result.isEnabled())
                .isTrue();

        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(userRepository)
                .findByUsername("admin");
    }

    @Test
    void shouldSupportMultipleRoles() {

        User user = new User(
                "user-id",
                "admin",
                "$2a$10$hashedPassword",
                Set.of(Role.USER, Role.ADMIN),
                true
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_USER",
                        "ROLE_ADMIN"
                );
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("unknown")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: unknown");
    }

    @Test
    void shouldReturnDisabledUser() {

        User user = new User(
                "user-id",
                "disabled",
                "$2a$10$hashedPassword",
                Set.of(Role.USER),
                false
        );

        when(userRepository.findByUsername("disabled"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("disabled");

        assertThat(result.isEnabled())
                .isFalse();
    }
}