package com.jasonvennin.compliance.infrastructure.persistence.mongo.user;

import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(MongoUserRepository.class)
class MongoUserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoUserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Test
    void shouldSaveAndFindUserByUsername() {

        User user = new User(
                null,
                "admin",
                "$2a$10$hashedPassword",
                Set.of(Role.ADMIN),
                true
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.id()).isNotNull();
        assertThat(savedUser.username()).isEqualTo("admin");
        assertThat(savedUser.passwordHash())
                .isEqualTo("$2a$10$hashedPassword");
        assertThat(savedUser.roles())
                .containsExactly(Role.ADMIN);
        assertThat(savedUser.enabled()).isTrue();

        var foundUser =
                userRepository.findByUsername("admin");

        assertThat(foundUser).isPresent();

        assertThat(foundUser.get().id())
                .isEqualTo(savedUser.id());
        assertThat(foundUser.get().username())
                .isEqualTo("admin");
        assertThat(foundUser.get().passwordHash())
                .isEqualTo("$2a$10$hashedPassword");
        assertThat(foundUser.get().roles())
                .containsExactly(Role.ADMIN);
        assertThat(foundUser.get().enabled())
                .isTrue();
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {

        var result =
                userRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSupportMultipleRoles() {

        User user = new User(
                null,
                "admin",
                "$2a$10$hashedPassword",
                Set.of(Role.USER, Role.ADMIN),
                true
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.roles())
                .containsExactlyInAnyOrder(
                        Role.USER,
                        Role.ADMIN
                );
    }
}