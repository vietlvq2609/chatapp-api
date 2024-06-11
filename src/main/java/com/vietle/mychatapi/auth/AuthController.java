package com.vietle.mychatapi.auth;


import com.vietle.mychatapi.response.dto.ApiResponseDTO;
import com.vietle.mychatapi.response.dto.ApiSuccessResponseDTO;
import com.vietle.mychatapi.user.dto.UserCreateDTO;
import com.vietle.mychatapi.user.dto.UserLoginDTO;
import com.vietle.mychatapi.user.dto.UserPasswordUpdateDTO;
import com.vietle.mychatapi.user.dto.UserResponseDTO;

import io.jsonwebtoken.Claims;

import com.vietle.mychatapi.jwt.JwtTokenUtil;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO> signup(@Valid @RequestBody UserCreateDTO requestUser) {
        UserResponseDTO newUser = userService.createUser(requestUser);

        UserLoginDTO loginUser = new UserLoginDTO();
        loginUser.setUsername(requestUser.getEmail());
        loginUser.setPassword(requestUser.getPassword());

        String token = jwtTokenUtil.getUserToken(loginUser);

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("userInfo", newUser);

        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(HttpStatus.CREATED, payload);
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDTO> signin(@Valid @RequestBody UserLoginDTO user) {
        UserResponseDTO userInfo = userService.validateAndReturnUser(user);
        String token =  jwtTokenUtil.getUserToken(user);

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("userInfo", userInfo);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(httpStatus, payload);

        return new ResponseEntity<>(responseObject, httpStatus);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO> refreshToken(@Valid @RequestBody String token) {
        String refreshToken = jwtTokenUtil.refreshToken(token);

        Map<String, String> payload = new HashMap<>();
        payload.put("refreshToken", refreshToken);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(httpStatus, payload);

        return new ResponseEntity<>(responseObject, httpStatus);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponseDTO> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody UserPasswordUpdateDTO dto
    ) {
        String jwtToken = request.getHeader("Authorization");
        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        Long userId = Long.parseLong(String.valueOf(claims.get("user_id")));

        userService.updateUserPassword(userId, dto.getCurrentPassword(), dto.getNewPassword());

        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Password is updated successfully!");

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO responseObject = new ApiSuccessResponseDTO(httpStatus, payload);

        return new ResponseEntity<>(responseObject, httpStatus);
    }
}