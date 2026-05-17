package com.microservices.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

//    This DTO represents the request payload for user registration,
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Enter valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}