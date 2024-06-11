package com.vietle.mychatapi.jwt;

import com.vietle.mychatapi.user.dto.UserLoginDTO;
import com.vietle.mychatapi.user.User;
import com.vietle.mychatapi.exception.ApiErrorType;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class JwtTokenUtil {
    private final UserRepository userRepository;
    private final SecretKey secretKey;
    private final int expirationTimeInMs;

    public JwtTokenUtil(UserRepository userRepository, @Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expirationTime}")  int expirationTimeInMs) {
        byte[] secretBytes = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationTimeInMs = expirationTimeInMs;
        this.userRepository = userRepository;
    }

    public String getUserToken(UserLoginDTO userDto) {
        User user = getUserFromDb(userDto);
        return generateToken(user);
    }

    public Claims decodeToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return claimsJws.getPayload();
        } catch (ExpiredJwtException e) {
            throw new ApiException("JWT token is expired!", HttpStatus.BAD_REQUEST, ApiErrorType.JWT_EXPIRED);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeInMs);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String refreshToken(String token) {
        Claims claims = decodeToken(token);
        Date now = new Date();
        Date newExpirationDate = new Date(now.getTime() + expirationTimeInMs);

        Map<String, Object> updatedClaims = Map.of(
            "user_id", claims.get("user_id"),
            "username", claims.get("username"),
            "email", claims.get("email")
        );

        return Jwts.builder()
            .claims(updatedClaims)
            .issuedAt(now)
            .expiration(newExpirationDate)
            .signWith(secretKey)
            .compact();
    }

    // PRIVATES METHOD DEFINITIONS
    private User getUserFromDb(UserLoginDTO user) {
        Optional<User> query = userRepository.findByEmail(user.getUsername());
        if (query.isEmpty()) {
            throw new ApiException("User not found! Please check your username!", HttpStatus.NO_CONTENT);
        }
        return query.get();
    }
}
