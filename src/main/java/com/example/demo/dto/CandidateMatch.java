package com.example.demo.dto;

import com.example.demo.entity.JobDescription;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class CandidateMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id")
    private JobDescription jobDescription;

    @Lob
    private String resumeText;

    private double score;

    private LocalDateTime uploadedAt;
}

