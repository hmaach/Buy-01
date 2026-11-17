package com.buy01.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AvatarServiceImplTest {

    // The first 8 bytes of every PNG file. Tika recognises the type from them, not from the file name.
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    @TempDir
    private Path uploadDir;

    private AvatarServiceImpl avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarServiceImpl(uploadDir.toString());
        avatarService.init();
    }

    @Test
    void saveAvatar_rejects_file_over_2mb() throws IOException {
        byte[] tooBig = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("avatar", "big.png", "image/png", tooBig);

        assertThrows(IllegalArgumentException.class, () -> avatarService.saveAvatar(file));

        assertEquals(0, countStoredFiles());
    }

    @Test
    void saveAvatar_rejects_non_image_content_even_with_png_name() throws IOException {
        byte[] text = "this is not an image".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("avatar", "photo.png", "image/png", text);

        assertThrows(IllegalArgumentException.class, () -> avatarService.saveAvatar(file));

        assertEquals(0, countStoredFiles());
    }

    @Test
    void saveAvatar_stores_png_and_returns_avatars_url() {
        MockMultipartFile file = new MockMultipartFile("avatar", "me.png", "image/png", PNG_SIGNATURE);

        String url = avatarService.saveAvatar(file);

        assertTrue(url.startsWith("/avatars/") && url.endsWith(".png"));
        String storedName = url.substring("/avatars/".length());
        assertTrue(Files.exists(uploadDir.resolve(storedName)));
    }

    private long countStoredFiles() throws IOException {
        try (Stream<Path> files = Files.list(uploadDir)) {
            return files.count();
        }
    }
}
