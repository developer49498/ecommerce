package com.example.isms.model;

import lombok.Data;

@Data
public class CourseResult {
    private String name;
    private int credit;
    private double GP_before_penalty;
    private double penalty;
    private double net_gp;
    private String grade;
}