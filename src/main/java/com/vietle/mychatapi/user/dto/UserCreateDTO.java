package com.vietle.mychatapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.util.Date;

@Data
public class UserCreateDTO {

    @NotBlank(message = "Username is required!")
    private String username;

    @NotBlank(message = "Password is required!")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Phone number is required!")
    @Size(min = 10, max = 11, message = "Phone number must be between 10 and 11 characters")
    private String phoneNumber;

    @NotBlank(message = "Email is required!")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Date of birth is required!")
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
}