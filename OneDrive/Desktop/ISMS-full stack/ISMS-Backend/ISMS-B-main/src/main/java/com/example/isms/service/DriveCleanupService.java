package com.example.isms.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.model.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DriveCleanupService {

    private final DriveService driveService;
    private final String folderId = "1Ro04anCJN4_v1QQA3JZMGmRJe4pBBfva";

    public DriveCleanupService(DriveService driveService) {
        this.driveService = driveService;
    }

    @Scheduled(cron = "0 0 0 * * SUN") // Every Sunday at midnight
    public void cleanupOldFiles() {
        try {
            List<File> files = driveService.listFilesInFolder(folderId);
            Instant sixMonthsAgo = Instant.now().minus(180, ChronoUnit.DAYS);

            for (File file : files) {
                // Google Drive file's createdTime or modifiedTime is in RFC 3339 format
                Instant fileCreatedTime = Instant.parse(file.getCreatedTime().toStringRfc3339());

                if (fileCreatedTime.isBefore(sixMonthsAgo)) {
                    driveService.deleteFile(file.getId());
                    System.out.println("Deleted old file: " + file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

