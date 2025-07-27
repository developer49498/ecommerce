package com.example.isms.model;

import java.util.List;

public class HostelMappingRequest {
    private List<String> studentIds;
    private String hostel; // "new-hostel" or "old-hostel"

    // Getters and setters


    public HostelMappingRequest() {
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }

    public String getHostel() {
        return hostel;
    }

    public void setHostel(String hostel) {
        this.hostel = hostel;
    }
}

