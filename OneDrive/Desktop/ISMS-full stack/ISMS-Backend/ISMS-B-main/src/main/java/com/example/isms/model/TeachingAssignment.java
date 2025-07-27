package com.example.isms.model;


public class TeachingAssignment {

    private String classId;
    private String subject;

    public TeachingAssignment() {
    }

    public TeachingAssignment(String classId, String subject) {
        this.classId = classId;
        this.subject = subject;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
