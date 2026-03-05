package com.buy01.media.domain.ports.inbound;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.media.domain.model.Media;

public interface MediaUseCase {
        Media uploadImage(MultipartFile file, String userId);

        Resource getImageFile(String mediaId);

        MediaType detectContentType(Resource resource);

        List<String> getProdutImages(String productId);

        Map<String, String> findImageUrlsByProductIds(Collection<String> productIds);

        void deleteById(String id, String userId);
}
