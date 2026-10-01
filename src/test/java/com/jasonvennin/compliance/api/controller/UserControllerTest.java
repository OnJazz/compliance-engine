package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    void shouldReturnCurrentUser() {

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        UserController controller =
                new UserController(userRepository);

        User user = new User(
                "user-1",
                "admin",
                "hashed-password",
                Set.of(Role.ADMIN, Role.USER),
                true
        );

        when(authentication.getName())
                .thenReturn("admin");

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        var response = controller.me(authentication);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                "admin",
                response.getBody().username()
        );

        assertEquals(
                true,
                response.getBody().enabled()
        );

        verify(authentication)
                .getName();

        verify(userRepository)
                .findByUsername("admin");
    }

    @Test
    void shouldThrowWhenCurrentUserDoesNotExist() {

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        UserController controller =
                new UserController(userRepository);

        when(authentication.getName())
                .thenReturn("unknown");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> controller.me(authentication)
        );

        verify(userRepository)
                .findByUsername("unknown");
    }
}