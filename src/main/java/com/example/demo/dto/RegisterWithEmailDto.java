package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class RegisterWithEmailDto {
    @NotEmpty
    @Size(min = 2, max = 70, message = "Name can't be less then 2 and greater then 70 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Invalid Name format")
    private String name;


    private String address = null;

    private User.UserType userRole = null;

    @NotBlank(message = "Email address is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$", message = "Invalid email address format")
    @Size(max = 254, message = "Email address is too long (maximum 254 characters)")
    private String email;



    @NotNull(message = "Mobile number cannot be null")
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 characters")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number format")
    private String mobileNo;

    @NotNull
    @NotEmpty
    private String password;

    @NotNull
    @NotEmpty
    private String comfirmPassword;

}
