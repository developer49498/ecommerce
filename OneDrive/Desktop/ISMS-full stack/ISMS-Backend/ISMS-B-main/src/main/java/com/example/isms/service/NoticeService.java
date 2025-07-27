package com.example.isms.service;

import com.example.isms.model.Group;
import com.example.isms.model.Notice;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class NoticeService {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("notice");

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public CompletableFuture<String> createNoticeAsync(Notice notice) {
        String noticeId = UUID.randomUUID().toString();
        notice.setNoticeId(noticeId);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String dayWithSuffix = getDayWithSuffix(now.getDayOfMonth());
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
        String formattedDateTime = dayWithSuffix + " " + monthYear + " " + time;
        notice.setPostedOn(formattedDateTime);

        try {
            // Save notice to Firebase (blocking but done in async thread)
            ApiFuture<Void> writeFuture = dbRef.child(noticeId).setValueAsync(notice);

            // Send email notification (still async)
            Group group = new GroupService().getGroupByName(notice.getAttention());
            if (group != null && group.getGroupMail() != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(group.getGroupMail());
                message.setSubject("New notice update from " + notice.getPostedBy());
                message.setText("Title: " + notice.getTitle() + "\nTo know more, visit iiit-bh.ac.in");
                mailSender.send(message);
            }

            return CompletableFuture.completedFuture(noticeId);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<Notice>> getAllNotices() {
        CompletableFuture<List<Notice>> future = new CompletableFuture<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Notice> notices = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notice notice = child.getValue(Notice.class);
                    if (notice != null) {
                        notices.add(notice);
                    }
                }

                // Sort notices by postedOn descending (most recent first)
                notices.sort((n1, n2) -> {
                    ZonedDateTime d1 = parsePostedOn(n1.getPostedOn());
                    ZonedDateTime d2 = parsePostedOn(n2.getPostedOn());
                    // Newest first
                    return d2.compareTo(d1);
                });

                future.complete(notices);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to fetch notices"));
            }
        });

        return future;
    }

    // Helper method to parse postedOn string to ZonedDateTime
    private ZonedDateTime parsePostedOn(String postedOn) {
        if (postedOn == null) return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Asia/Kolkata"));

        try {
            // Remove day suffix (e.g., "19th" -> "19")
            String dayPart = postedOn.split(" ")[0].replaceAll("(st|nd|rd|th)", "");
            String rest = postedOn.substring(postedOn.indexOf(" ") + 1);

            // Compose new string without suffix, e.g. "19 May 2025 03:45 PM"
            String dateStr = dayPart + " " + rest;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy hh:mm a").withZone(ZoneId.of("Asia/Kolkata"));
            return ZonedDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            // Fallback: epoch date if parsing fails
            return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Asia/Kolkata"));
        }
    }


    private String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) return day + "th";
        switch (day % 10) {
            case 1:
                return day + "st";
            case 2:
                return day + "nd";
            case 3:
                return day + "rd";
            default:
                return day + "th";
        }
    }

}
