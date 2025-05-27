package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class ResumeResult {
    private String fileName;
    private String name;
    private List<String> skills;
    private List<Map<String, String>> experience;
    private double score;
    private String summary;
    private List<String> education;
    private List<String> achievements;
    public ResumeResult(){}
}
