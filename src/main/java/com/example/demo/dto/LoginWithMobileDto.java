package com.example.demo.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginWithMobileDto {

    @NotNull(message = "country code cannot be null")
    private String country_code_id;

    @NotNull(message = "Mobile number cannot be null")
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 characters")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number format")
    private String mobileNo;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 4, message = "OTP number must be exactly 4 characters")
    private String otp;
}
