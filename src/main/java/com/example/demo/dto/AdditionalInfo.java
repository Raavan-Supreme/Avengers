package com.example.demo.dto;

import lombok.Data;

import java.util.List;
@Data
public class AdditionalInfo {
    private List<String> hobbies;
    private List<String> volunteer;
    private String references;
}

