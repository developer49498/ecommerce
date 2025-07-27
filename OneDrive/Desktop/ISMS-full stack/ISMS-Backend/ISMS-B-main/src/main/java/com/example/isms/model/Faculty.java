package com.example.isms.model;

import java.util.List;
import java.util.Map;

public class Faculty {

    private String name;
    private String id;
    private String department;
    private String experience;
    private String qualification;
    private String contact;
    private String designation;
    private String lastLogin; // + getter and setter

    private String administrative_position;

    // Map<classId, subject>
    private List<TeachingAssignment> currentlyTeaching;
    public Faculty() {
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Faculty(String name, String id, String department, String experience,
                   String qualification, String contact,
                   String designation, String administrative_position, List<TeachingAssignment> currentlyTeaching) {
        this.name = name;
        this.id = id;
        this.department = department;
        this.experience = experience;
        this.qualification = qualification;
        this.contact = contact;
        this.designation = designation;
        this.administrative_position=administrative_position;
        this.currentlyTeaching =currentlyTeaching;
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<TeachingAssignment> getCurrentlyTeaching() {
        return currentlyTeaching;
    }

    public void setCurrentlyTeaching(List<TeachingAssignment> currentlyTeaching) {
        this.currentlyTeaching = currentlyTeaching;
    }

    public String getAdministrative_position() {
        return administrative_position;
    }

    public void setAdministrative_position(String administrative_position) {
        this.administrative_position = administrative_position;
    }
}
