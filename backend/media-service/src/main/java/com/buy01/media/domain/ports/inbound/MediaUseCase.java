package com.buy01.media.domain.ports.inbound;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.media.domain.model.Media;

public interface MediaUseCase {
        Media uploadImage(MultipartFile file);

        Resource getImageFile(String mediaId);

        MediaType guessContentType(String filename);
}
