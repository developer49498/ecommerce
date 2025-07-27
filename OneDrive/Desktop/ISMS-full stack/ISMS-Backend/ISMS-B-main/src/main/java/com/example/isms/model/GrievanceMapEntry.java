package com.example.isms.model;

public class GrievanceMapEntry {
    private boolean resolveAccess;
    private String status; // "pending", "resolved", "forwarded"

    public GrievanceMapEntry() {}
    // Getters and setters


    public GrievanceMapEntry(boolean resolveAccess, String status) {
        this.resolveAccess = resolveAccess;
        this.status = status;
    }

    public boolean isResolveAccess() {
        return resolveAccess;
    }

    public void setResolveAccess(boolean resolveAccess) {
        this.resolveAccess = resolveAccess;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

