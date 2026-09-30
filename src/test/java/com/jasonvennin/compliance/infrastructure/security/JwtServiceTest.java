package com.jasonvennin.compliance.infrastructure.security;

import com.jasonvennin.compliance.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void shouldGenerateTokenWithRoles() {

        JwtEncoder jwtEncoder = mock(JwtEncoder.class);

        Jwt jwt = mock(Jwt.class);

        when(jwt.getTokenValue())
                .thenReturn("jwt-token");

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        JwtService jwtService =
                new JwtService(jwtEncoder);

        String token = jwtService.generateToken(
                "admin",
                Set.of(Role.ADMIN, Role.USER)
        );

        assertThat(token)
                .isEqualTo("jwt-token");
    }
}