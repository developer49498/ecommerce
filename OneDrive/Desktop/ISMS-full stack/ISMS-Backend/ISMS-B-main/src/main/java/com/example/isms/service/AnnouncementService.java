package com.example.isms.service;

import com.example.isms.model.Announcement;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AnnouncementService {

    public CompletableFuture<String> postAnnouncementToRealtimeDB(String semester, String courseId, Announcement announcement) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("announcement/" + semester + "_" + courseId + "/entries").push();

        Map<String, Object> data = new HashMap<>();
        data.put("announcement_content", announcement.getAnnouncement_content());
        data.put("posted_by", announcement.getPosted_by());
        data.put("posted_on", announcement.getPosted_on());

        if (announcement.getAttachment() != null) {
            data.put("attachment", announcement.getAttachment());
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        ref.setValueAsync(data).addListener(() -> {
            future.complete(ref.getKey());
        }, Runnable::run);

        return future;
    }


    public CompletableFuture<List<Announcement>> getAnnouncements(String semester, String courseId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("announcement/" + semester + "_" + courseId + "/entries");

        CompletableFuture<List<Announcement>> future = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Announcement> announcements = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Announcement announcement = child.getValue(Announcement.class);
                    announcements.add(announcement);
                }

                // Sort by posted_on descending (assuming format "dd-MM-yyyy HH:mm:ss")
                announcements.sort((a, b) -> {
                    try {
                        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                .parse(b.getPosted_on())
                                .compareTo(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                        .parse(a.getPosted_on()));
                    } catch (Exception e) {
                        return 0; // fallback if parsing fails
                    }
                });

                future.complete(announcements);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Database error: " + error.getMessage()));
            }
        });

        return future;
    }
}
