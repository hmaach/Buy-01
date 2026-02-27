package com.buy01.media.infrastructure.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.buy01.media.domain.model.FileStatus;

import lombok.Data;

@Data
@Document(collection = "medias")
public class MediaDocument {
    @Id
    private String id;

    @Field("image_path")
    private String imagePath;

    @Field("product_id")
    private String productId;

    private FileStatus status;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}
