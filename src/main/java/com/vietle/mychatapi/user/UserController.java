package com.vietle.mychatapi.user;

import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.jwt.JwtTokenUtil;
import com.vietle.mychatapi.response.dto.ApiResponseDTO;
import com.vietle.mychatapi.response.dto.ApiSuccessResponseDTO;
import com.vietle.mychatapi.user.dto.UserResponseDTO;
import com.vietle.mychatapi.user.dto.UserUpdateDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAll() {
        Collection<UserResponseDTO> userList = userService.getAll();
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(HttpStatus.OK, userList);
        return new ResponseEntity<>(responseObject, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO> getOne(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(HttpStatus.OK, user);
        return new ResponseEntity<>(responseObject, HttpStatus.OK);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO> updateUser(
            @Valid @RequestParam("user_id") String pUserId,
            @Valid @RequestParam("username") String pUsername,
            @Valid @RequestParam("email") String pEmail,
            @Valid @RequestParam("phone") String pPhone,
            @Valid @RequestParam("birth") String pBirth,
            @Valid @RequestParam(value = "profile_photo", required = false) MultipartFile pProfilePhoto,
            @PathVariable Long userId,
            HttpServletRequest request
    ) throws IOException, java.io.IOException {
        String jwtToken = request.getHeader("Authorization");
        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        Long userIdClaim = Long.parseLong(String.valueOf(claims.get("user_id")));

        if (!userIdClaim.equals(userId) || !userId.equals(Long.parseLong(pUserId))) {
            throw new ApiException("User ID is not match!", HttpStatus.FORBIDDEN);
        }

        UserUpdateDTO dto = UserUpdateDTO.builder()
            .userId(Long.parseLong(pUserId))
            .username(pUsername)
            .email(pEmail)
            .phoneNumber(pPhone)
            .profilePhoto(pProfilePhoto)
            .build();

        UserResponseDTO payload = userService.updateUser(dto);

        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(HttpStatus.OK, payload);
        return new ResponseEntity<>(responseObject, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(HttpStatus.OK, String.format("User %d deleted! ", userId));
        return new ResponseEntity<>(responseObject, HttpStatus.OK);
    }
}
