package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class InterviewInvitationRequest {
    private List<String> emails;
    private String candidateName;
    private String interviewDate; // e.g. "March 3rd, 2023"
    private String interviewLocation; // e.g. "Devstringx Pvt Ltd\n3rd Floor, Magnus Tower\nNoida"
}

