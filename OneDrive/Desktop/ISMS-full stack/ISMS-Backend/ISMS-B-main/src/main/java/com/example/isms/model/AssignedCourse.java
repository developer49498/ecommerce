package com.example.isms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignedCourse {
    private String courseId;
    private String courseName;
    private String facultyName;
    private String semester;
    private String roomNumber; // general room location
    private int credits;
    private Map<String, SectionInfo> branchSection;// e.g., {"CSE_A": "A-203", "CSE_B": "B-102"}
}

