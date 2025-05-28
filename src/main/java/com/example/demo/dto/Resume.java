package com.example.demo.dto;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> extraInfo = new HashMap<>();

    @JsonAnySetter
    public void setExtraInfo(String key, Object value) {
        extraInfo.put(key, value);
    }}

