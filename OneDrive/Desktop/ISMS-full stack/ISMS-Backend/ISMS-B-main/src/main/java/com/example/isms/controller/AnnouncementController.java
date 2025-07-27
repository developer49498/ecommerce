package com.example.isms.controller;

import com.example.isms.model.Announcement;
import com.example.isms.model.EmailRequest;
import com.example.isms.service.AnnouncementService;
import com.example.isms.service.DriveService;
import com.example.isms.service.GroupService;
import com.example.isms.model.Group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DriveService driveService;

    @Autowired
    private JavaMailSender mailSender;

    private final String folderId = "1Ro04anCJN4_v1QQA3JZMGmRJe4pBBfva"; // Replace with actual folder ID

    @PostMapping("/create")
    public String createAnnouncement(
            @RequestPart("announcement") Announcement announcement,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam String branchSection,
            @RequestParam String batchYear,
            @RequestParam String semester,
            @RequestParam String courseId
    ) {
        try {
            // Process file upload if present
            if (file != null && !file.isEmpty()) {
                File localFile = convertToFile(file);
                driveService.uploadFile(localFile, folderId);
                String link = driveService.getSharableLinkByFileName(localFile.getName(), folderId);
                announcement.setAttachment(link);
                localFile.delete();
            }

            // Add timestamp
            announcement.setPosted_on(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

            String id = announcementService.postAnnouncementToRealtimeDB(semester, courseId, announcement).get();


            // Retrieve email and send
            String groupName = branchSection + "_" + batchYear;
            Group group = groupService.getGroupByName(groupName);
            if (group != null) {
                sendEmail(group.getGroupMail(), "New Class Announcement", announcement.getAnnouncement_content());
            }

            return "Announcement created with ID: " + id;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private File convertToFile(MultipartFile multipartFile) throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    @GetMapping("/get")
    public CompletableFuture<List<Announcement>> getAnnouncements(
            @RequestParam String semester,
            @RequestParam String courseId
    ) {
        return announcementService.getAnnouncements(semester, courseId);
    }

}
