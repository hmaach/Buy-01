package com.buy01.user.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.domain.port.in.AvatarService;

import jakarta.annotation.PostConstruct;

@Service
public class AvatarServiceImpl implements AvatarService {

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    private final Tika tika = new Tika();

    @Value("${app.avatar.upload-dir:./uploads/avatars}")
    private String uploadDir;

    private Path avatarPath;

    @Override
    @PostConstruct
    public void init() {
        this.avatarPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(avatarPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create avatar upload directory", e);
        }
    }

    @Override
    public String saveAvatar(MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }

        // Validate file size
        if (avatar.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds 2MB limit. Received: " + avatar.getSize() + " bytes");
        }

        // Validate MIME type using Tika
        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(avatar.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read file for type detection: " + e.getMessage());
        }

        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Detected: " + detectedMimeType
                    + ". Only JPEG, PNG, GIF, and WebP are allowed.");
        }

        String originalFilename = avatar.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = avatarPath.resolve(filename);

        try {
            Files.copy(avatar.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar", e);
        }

        return "/avatars/" + filename;
    }

    @Override
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
        Path filePath = avatarPath.resolve(filename);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log the error but don't fail the operation
            System.err.println("Failed to delete avatar: " + avatarUrl);
        }
    }
}
