package com.example.demo;

import com.example.demo.dto.InterviewInvitationRequest;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-invitations")
    public ResponseEntity<Map<String, String>> sendInterviewInvitations(@RequestBody InterviewInvitationRequest request) {
        for (String email : request.getEmails()) {
            emailService.sendInterviewInvitation(email, request.getCandidateName(), request.getInterviewDate(), request.getInterviewLocation());
        }
        return ResponseEntity.ok(Map.of("message", "Interview invitations sent successfully."));
    }
}

