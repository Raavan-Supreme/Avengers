package com.example.demo.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Resume {
    @JsonProperty("personal_info")
    private PersonalInfo personal_info;
    private String summary;
    private Skills skills;
    private List<Experience> experience;
    private List<Education> education;
    private List<Certification> certifications;
    private List<Project> projects;
    private List<String> achievements;
    @JsonProperty("additional_info")
    private AdditionalInfo additional_info;

    // Getters and Setters
}

