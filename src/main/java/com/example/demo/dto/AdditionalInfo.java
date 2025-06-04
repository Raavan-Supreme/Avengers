package com.example.demo.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class AdditionalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ElementCollection
    @CollectionTable(name = "additional_info_hobbies", joinColumns = @JoinColumn(name = "additional_info_id"))
    private List<String> hobbies;

    @ElementCollection
    @CollectionTable(name = "additional_info_volunteer", joinColumns = @JoinColumn(name = "additional_info_id"))
    private List<String> volunteer;
    @Column(name = "reference_info")
    private String references;
}

