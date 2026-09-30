package com.jasonvennin.compliance.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleAuthenticationConverterTest {

    private final JwtRoleAuthenticationConverter converter =
            new JwtRoleAuthenticationConverter();

    @Test
    void shouldConvertRolesToAuthorities() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("admin")
                .claim("roles", List.of("ADMIN", "USER"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var authentication =
                converter.convert(jwt);

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_ADMIN",
                        "ROLE_USER"
                );

        assertThat(authentication.getName())
                .isEqualTo("admin");
    }

    @Test
    void shouldReturnNoAuthoritiesWhenRolesAreMissing() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var authentication =
                converter.convert(jwt);

        assertThat(authentication.getAuthorities())
                .isEmpty();
    }
}