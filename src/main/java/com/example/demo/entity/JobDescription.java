package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "experience_required")
    private String experienceRequired;

    @OneToOne(mappedBy = "jobDescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private JobSkills skills;
}

