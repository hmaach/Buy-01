package com.buy01.media.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataProductRepository extends MongoRepository<MediaDocument, String> {

}
