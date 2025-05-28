package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class Skills {
    private List<String> technical;
    private List<String> soft;
    private List<String> tools;
    private List<String> languages;

    // Getters and Setters
}

