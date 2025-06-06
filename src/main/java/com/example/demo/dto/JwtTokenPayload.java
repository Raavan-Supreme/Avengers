package com.example.demo.dto;



import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class JwtTokenPayload {
    private String userId;
    private String fullName;
    private String email;
    private String mobileNo;
    private User.UserType userType;
}