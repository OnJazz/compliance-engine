package com.jasonvennin.compliance.infrastructure.persistence.mongo.user;

import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoUserRepository implements UserRepository {

    private final SpringDataUserRepository repository;

    public MongoUserRepository(
            SpringDataUserRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository
                .findByUsername(username)
                .map(UserDocument::toDomain);
    }

    @Override
    public User save(User user) {
        UserDocument document = UserDocument.fromDomain(user);

        return repository
                .save(document)
                .toDomain();
    }
}