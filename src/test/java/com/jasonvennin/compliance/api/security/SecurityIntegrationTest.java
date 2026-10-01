package com.jasonvennin.compliance.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("dev")
class SecurityIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenAccessingProtectedEndpointWithoutToken()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void shouldAllowUserToAccessUserEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("admin"))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_USER")
                                )
                        )
        ).andExpect(
                status().isOk()
        );
    }

    @Test
    void shouldAllowAdminToAccessUserEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("admin"))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
        ).andExpect(
                status().isOk()
        );
    }

    @Test
    void shouldReturn403WhenUserAccessesAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/users")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("admin"))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_USER")
                                )
                        )
        ).andExpect(
                status().isForbidden()
        );
    }
}