package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "job_skills")
public class JobSkills {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "technical_skills", joinColumns = @JoinColumn(name = "job_skills_id"))
    @Column(name = "skill")
    private List<String> technical = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tools", joinColumns = @JoinColumn(name = "job_skills_id"))
    @Column(name = "tool")
    private List<String> tools = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "methodologies", joinColumns = @JoinColumn(name = "job_skills_id"))
    @Column(name = "methodology")
    private List<String> methodologies = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "job_description_id")
    private JobDescription jobDescription;
}

