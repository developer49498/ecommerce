package com.example.isms.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ForwardingChainEntry {
    private String forwardedFrom;
    private String forwardedTo;
    private long forwardedTime; // epoch milliseconds
    private String forwardingNote; // NEW FIELD

    public ForwardingChainEntry() {}

    public ForwardingChainEntry(String forwardedFrom, String forwardedTo, long forwardedTime, String forwardingNote) {
        this.forwardedFrom = forwardedFrom;
        this.forwardedTo = forwardedTo;
        this.forwardedTime = forwardedTime;
        this.forwardingNote = forwardingNote;
    }

    // Getters and setters
    public String getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(String forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }

    public String getForwardedTo() {
        return forwardedTo;
    }

    public void setForwardedTo(String forwardedTo) {
        this.forwardedTo = forwardedTo;
    }

    public long getForwardedTime() {
        return forwardedTime;
    }

    public void setForwardedTime(long forwardedTime) {
        this.forwardedTime = forwardedTime;
    }

    public String getForwardingNote() {
        return forwardingNote;
    }

    public void setForwardingNote(String forwardingNote) {
        this.forwardingNote = forwardingNote;
    }

    // Method to return formatted time
    public String getFormattedForwardedTime() {
        ZonedDateTime dateTime = Instant.ofEpochMilli(forwardedTime)
                .atZone(ZoneId.of("Asia/Kolkata")); // UTC+5:30

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm:ss z");
        return dateTime.format(formatter);
    }
}
