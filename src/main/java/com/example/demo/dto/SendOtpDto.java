package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Data
public class SendOtpDto {

    @NotNull
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 characters")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number format")
    private String mobileNo;
    private String email;

    private SentOtp.OtpType otpType;

    private String userId;

}

