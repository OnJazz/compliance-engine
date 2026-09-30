package com.jasonvennin.compliance.infrastructure.persistence.mongo.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserRepository
        extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByUsername(String username);
}
