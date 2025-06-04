package com.example.demo.dto;

import com.example.demo.service.MapToJsonConverter;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String company;

    @JsonAlias({"position"})
    private String role;

    private String duration;

    @ElementCollection
    private List<String> description;

    @ElementCollection
    private List<String> achievements;


    private String location;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> extraInfo = new HashMap<>();

    @JsonAnySetter
    public void setExtraInfo(String key, Object value) {
        extraInfo.put(key, value);
    }
}
