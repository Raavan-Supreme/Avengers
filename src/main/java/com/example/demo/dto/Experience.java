package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Experience {
    private String company;
    @JsonAlias({"position"})
    private String role;
    private String duration;
    private List<String> description;
    private List<String> achievements;
    private String location;

    private Map<String, Object> extraInfo = new HashMap<>();

    @JsonAnySetter
    public void setExtraInfo(String key, Object value) {
        extraInfo.put(key, value);
    }
}

