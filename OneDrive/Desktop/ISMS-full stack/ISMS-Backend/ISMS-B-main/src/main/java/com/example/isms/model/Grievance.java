package com.example.isms.model;

public class Grievance {
    private String id; // Auto-generated
    private String title;
    private String content;
    private String from; // Student ID
    private String addressedTo; // Faculty name
    private long timestamp;
    private String finalStatus; // Optional, used when resolved

    public Grievance() {} // Required for Firebase
    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAddressedTo() {
        return addressedTo;
    }

    public void setAddressedTo(String addressedTo) {
        this.addressedTo = addressedTo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}
