package com.buy01.media.infrastructure.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buy01.media.domain.model.FileStatus;

public interface SpringDataProductRepository extends MongoRepository<MediaDocument, String> {
    List<MediaDocument> findByStatusAndCreatedAtBefore(FileStatus status, Instant threshold);

}
