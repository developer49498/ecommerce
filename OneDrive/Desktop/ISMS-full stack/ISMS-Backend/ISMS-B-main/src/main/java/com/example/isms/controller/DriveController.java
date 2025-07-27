package com.example.isms.controller;

import com.example.isms.service.DriveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final DriveService driveService;

    // Inject folder ID from application.properties/yml

    private String folderId="1Ro04anCJN4_v1QQA3JZMGmRJe4pBBfva";

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            // Convert MultipartFile to java.io.File
            File file = convertToFile(multipartFile);

            // Upload to Google Drive
            String link = driveService.uploadFile(file, folderId);

            // Clean up temp file
            file.delete();

            return ResponseEntity.ok(link);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    // Convert MultipartFile to java.io.File
    private File convertToFile(MultipartFile multipartFile) throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    @GetMapping("/link")
    public String getSharableLink(@RequestParam String fileName) {
        try {
            return DriveService.getSharableLinkByFileName(fileName, folderId);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

