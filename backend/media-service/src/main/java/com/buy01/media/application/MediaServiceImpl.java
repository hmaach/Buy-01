package com.buy01.media.application;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;
import com.buy01.media.domain.ports.inbound.MediaUseCase;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;
import com.buy01.media.infrastructure.web.exception.Errors.Faileduploadedfile;

@Service
public class MediaServiceImpl implements MediaUseCase {
    private final MediaRepositoryPort repository;

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final String UPLOAD_DIR = "uploads/";

    public MediaServiceImpl(MediaRepositoryPort repository) throws IOException {
        this.repository = repository;
        createUploadDir();
    }

    @Override
    public Resource getImageFile(String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media ID is required");
        }

        Media doc = repository.findById(mediaId);
        if (doc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
        }

        String pathStr = doc.getImagePath();
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
    public MediaType guessContentType(String filename) {
        if (filename == null)
            return MediaType.IMAGE_JPEG;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))
            return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp"))
            return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".gif"))
            return MediaType.IMAGE_GIF;
        return MediaType.IMAGE_JPEG;
    }

    @Override
    public Media uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new Faileduploadedfile("File is empty");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new Faileduploadedfile("File size exceeds 2MB limit. Received: " + file.getSize() + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Faileduploadedfile("Only image files are allowed");
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
                .status(FileStatus.PENDING)
                .build();

        try {
            return repository.save(media);
        } catch (Exception e) {
            tryDeleteFile(path);
            throw new Faileduploadedfile("Failed to save media metadata: " + e.getMessage());
        }
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
}
