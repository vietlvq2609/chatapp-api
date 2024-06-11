package com.vietle.mychatapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank(message = "User email is required for login!")
    @Email(message = "Invalid email format! Please check your username.")
    private String username;

    @NotBlank(message = "Password is required for login!")
    private String password;
}
