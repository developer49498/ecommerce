package com.example.isms.model;

public class GrievanceWithStatus {
    private Grievance grievance;
    private String status;

    // Constructors, getters, setters

    public GrievanceWithStatus() {
    }

    public GrievanceWithStatus(Grievance grievance, String status) {
        this.grievance = grievance;
        this.status = status;
    }

    public Grievance getGrievance() {
        return grievance;
    }

    public void setGrievance(Grievance grievance) {
        this.grievance = grievance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
