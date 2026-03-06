package com.buy01.media.application;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;
import com.buy01.media.domain.ports.inbound.MediaUseCase;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;
import com.buy01.media.infrastructure.web.exception.Errors.Faileduploadedfile;
import com.buy01.media.infrastructure.web.exception.Errors.NotFound;

@Service
public class MediaServiceImpl implements MediaUseCase {
    private final MediaRepositoryPort repository;

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final String UPLOAD_DIR = "uploads/";
    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    public MediaServiceImpl(MediaRepositoryPort repository) throws IOException {
        this.repository = repository;
        createUploadDir();
    }

    @Override
    public Resource getImageFile(String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media ID is required");
        }

        Optional<Media> doc = repository.findById(mediaId);
        if (doc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
        }

        String pathStr = doc.get().getImagePath();
        if (pathStr == null || pathStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Media has no stored path");
        }

        Path path = Paths.get(pathStr);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image file not found on disk");
            }
            if (!resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Image file is not readable");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path format", e);
        }
    }

    @Override
    public MediaType detectContentType(Resource resource) {
        try {
            String mimeType = tika.detect(resource.getInputStream());
            return MediaType.parseMediaType(mimeType);
        } catch (IOException e) {
            return MediaType.IMAGE_JPEG;
        }
    }

    @Override
    public Media uploadImage(MultipartFile file, String userId) {
        if (file.isEmpty()) {
            throw new Faileduploadedfile("File is empty");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new Faileduploadedfile("File size exceeds 2MB limit. Received: " + file.getSize() + " bytes");
        }

        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(file.getBytes());
        } catch (IOException e) {
            throw new Faileduploadedfile("Could not read file for type detection: " + e.getMessage());
        }

        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new Faileduploadedfile(
                    "Invalid file type. Detected: " + detectedMimeType
                            + ". Only JPEG, PNG, GIF, and WebP are allowed.");
        }

        String originalFilename = file.getOriginalFilename();
        String mediaId = UUID.randomUUID().toString();
        String extension = getExtension(originalFilename);
        String storagePath = UPLOAD_DIR + mediaId + extension;
        Path path = Paths.get(storagePath);

        try {
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            tryDeleteFile(path);
            throw new Faileduploadedfile("Failed to store file on disk: " + e.getMessage());
        }

        Media media = Media.builder()
                .imagePath(storagePath)
                .userId(userId)
                .status(FileStatus.PENDING)
                .build();

        try {
            return repository.save(media);
        } catch (Exception e) {
            tryDeleteFile(path);
            throw new Faileduploadedfile("Failed to save media metadata: " + e.getMessage());
        }
    }

    @Override
    public List<String> getProdutImages(String ProductId) {
        return repository.findByProductId(ProductId).stream().map(p -> p.getId()).toList();
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : "";
    }

    private void createUploadDir() throws IOException {
        Path p = Paths.get(UPLOAD_DIR);
        if (Files.notExists(p)) {
            Files.createDirectories(p);
        }
    }

    private void tryDeleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Failed to delete file: " + path + " - " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> findImageUrlsByProductIds(Collection<String> productIds) {
        List<Media> images = repository.findByProductIdIn(productIds);

        Map<String, String> urlMap = images.stream()
                .collect(Collectors.toMap(
                        Media::getProductId,
                        Media::getId,
                        (oldVal, newVal) -> oldVal));

        productIds.forEach(id -> urlMap.putIfAbsent(id, null));

        return urlMap;
    }

    @Override
    public void deleteById(String id, String userId) {
        if (id == null) {
            throw new IllegalArgumentException("id is ");
        }
        var media = repository.findById(id);
        if (media.isEmpty()) {
            throw new NotFound("media with id " + id + "no found");
        }
        if (!media.get().getUserId().equals(userId)) {
            throw new AccessDeniedException("Not Allowd to delete image");
        }

        repository.deleteById(id);
        var path = Paths.get(media.get().getImagePath());
        tryDeleteFile(path);
        System.out.println(".........> delete " + path);
    }
}
