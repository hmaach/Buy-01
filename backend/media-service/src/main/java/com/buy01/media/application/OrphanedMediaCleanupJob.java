package com.buy01.media.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.ports.outbound.MediaRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class OrphanedMediaCleanupJob {

    private final MediaRepositoryPort mediaRepo;

    private static final Duration ORPHAN_AGE = Duration.ofHours(1);

    @Scheduled(fixedRate = 3000) // every hour
    // @Scheduled(cron = "0 15 * * * *") // every hour at :15
    public void cleanupOrphanedPendingMedia() {
        Instant threshold = Instant.now().minus(ORPHAN_AGE);

        var medias = mediaRepo.findByStatusAndCreatedAtBefore(FileStatus.PENDING, threshold);
        if (medias.isEmpty())
            return;

        medias.forEach(media -> {
            var path = Paths.get(media.getImagePath());
            try {
                Files.deleteIfExists(path);
                mediaRepo.deleteById(media.getId());
                log.info("Cleaned up {} orphaned PENDING media items", media.getImagePath());
            } catch (IOException ex) {
                log.warn("Failed to delete file {}, keeping record", media.getImagePath());
            }
        });
    }
}
