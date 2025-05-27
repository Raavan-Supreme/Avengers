package com.example.demo.dto;

import lombok.Data;

import java.util.List;
@Data
public class AnalyzeRequest {
    private String jobTitle;
    private String jobDescription;
    private List<ResumeRequest> resumes;
}

