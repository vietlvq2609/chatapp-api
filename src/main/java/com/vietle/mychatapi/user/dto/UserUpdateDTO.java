package com.vietle.mychatapi.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;


@Data
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    @NotNull(message = "User ID is required!")
    private Long userId;

    @NotBlank(message = "Username is required!")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Size(min = 10, max = 11, message = "Phone number must be between 10 and 11 characters")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;

    private MultipartFile profilePhoto;
}
