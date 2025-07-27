package com.example.isms.model;

import lombok.Data;

import java.util.Map;

@Data
public class SemesterResult {
    private Map<String, CourseResult> courses;
    private FinalResult finalResult;

}