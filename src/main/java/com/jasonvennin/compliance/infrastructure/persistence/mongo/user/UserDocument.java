package com.jasonvennin.compliance.infrastructure.persistence.mongo.user;

import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
public record UserDocument(
        @Id
        String id,

        String username,

        String passwordHash,

        Set<Role> roles,

        boolean enabled
) {

    public static UserDocument fromDomain(User user) {
        return new UserDocument(
                user.id(),
                user.username(),
                user.passwordHash(),
                user.roles(),
                user.enabled()
        );
    }

    public User toDomain() {
        return new User(
                id,
                username,
                passwordHash,
                roles,
                enabled
        );
    }
}