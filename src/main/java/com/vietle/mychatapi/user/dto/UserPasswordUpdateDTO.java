package com.vietle.mychatapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordUpdateDTO {
    @NotBlank(message = "Password can not be empty")
    private String currentPassword;

    @NotBlank(message = "Password confirmation can not be empty")
    private String newPassword;
}
