package com.example.isms.controller;

import com.example.isms.model.Notice;
import com.example.isms.service.DriveService;
import com.example.isms.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private DriveService driveService;

    private String folderId = "1Ro04anCJN4_v1QQA3JZMGmRJe4pBBfva";

    @PostMapping("/create")
    public CompletableFuture<String> createNotice(
            @RequestPart("notice") Notice notice,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if (file != null && !file.isEmpty()) {
            try {
                File localFile = convertToFile(file);
                driveService.uploadFile(localFile, folderId);
                String previewLink = driveService.getSharableLinkByFileName(localFile.getName(), folderId);
                notice.setAttachment(previewLink);
                localFile.delete();
            } catch (Exception e) {
                CompletableFuture<String> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(e);
                return failedFuture;
            }
        }
        // Return the CompletableFuture directly without .get()
        return noticeService.createNoticeAsync(notice);
    }


    @GetMapping("/getAll")
    public List<Notice> getAllNotices() throws ExecutionException, InterruptedException {
        return noticeService.getAllNotices().get();
    }

    // Helper method
    private File convertToFile(MultipartFile multipartFile) throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}

