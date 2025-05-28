package com.example.demo.dto;

import lombok.Data;

@Data

public class Experience {
    private String company;  // Since your JSON has an empty experience array, add fields as needed later
    private String role;
    private String duration;
    private String description;

    // Getters and Setters
}

