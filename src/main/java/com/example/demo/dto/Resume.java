package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String phNo;
    private String email;
    private LocalDateTime uploadDate;

    @OneToOne(cascade = CascadeType.ALL)
    private PersonalInfo personal_info;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToOne(cascade = CascadeType.ALL)
    private Skills skills;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Experience> experience;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Education> education;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Certification> certifications;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Project> projects;

    @Lob
    @ElementCollection
    private List<String> achievements;

    @OneToOne(cascade = CascadeType.ALL)
    private AdditionalInfo additional_info;

    private Map<String, Object> extraInfo = new HashMap<>();

    private String compatibility;
    @Lob
    @Column
    private String criteria;
    private String status;
    private String rejectionReason;
    @JsonAnySetter
    public void setExtraInfo(String key, Object value) {
        extraInfo.put(key, value);
    }
}
