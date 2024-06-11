package com.vietle.mychatapi.conversation;

import com.vietle.mychatapi.conversation.dto.ConversationCreateDTO;
import com.vietle.mychatapi.conversation.dto.ConversationResponseDTO;
import com.vietle.mychatapi.response.dto.ApiResponseDTO;
import com.vietle.mychatapi.response.dto.ApiSuccessResponseDTO;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.jwt.JwtTokenUtil;
import com.vietle.mychatapi.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    public ConversationController(ConversationService conversationService, JwtTokenUtil jwtTokenUtil) {
        this.conversationService = conversationService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("{conversationId}")
    public ResponseEntity<ApiResponseDTO> getConversationById(
            @PathVariable Long conversationId,
            HttpServletRequest request) {
        ConversationResponseDTO responseDTO = conversationService.getConversationById(conversationId);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO payload = new ApiSuccessResponseDTO(httpStatus, responseDTO);

        return new ResponseEntity<>(payload, httpStatus);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllConversation(HttpServletRequest request) {
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            throw new ApiException("Please provide a valid authorization!", HttpStatus.BAD_REQUEST);
        }
        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        Long userId = ((Integer)claims.get("user_id")).longValue();

        List<ConversationResponseDTO> responseDTO = conversationService.getAllConversation(userId);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO payload = new ApiSuccessResponseDTO(httpStatus, responseDTO);
        return new ResponseEntity<>(payload, httpStatus);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO> createConversation(
            @Valid @RequestBody ConversationCreateDTO conversationCreateDTO,
            HttpServletRequest request) {

        // Validate creator authorization
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            throw new ApiException("Please provide a valid authorization!", HttpStatus.BAD_REQUEST);
        }
        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        String creatorId = String.valueOf(userService.getUserByEmail(conversationCreateDTO.getCreatorEmail()).getUserId());
        String userIdClaim = String.valueOf(claims.get("user_id"));
        if (!userIdClaim.equals(creatorId)) {
            throw new ApiException("Please provide a valid authorization header!", HttpStatus.BAD_REQUEST);
        }

        // Create conversation
        ConversationResponseDTO conversation = conversationService.createConversation(conversationCreateDTO);

        HttpStatus httpStatus = HttpStatus.CREATED;
        ApiResponseDTO payload = new ApiSuccessResponseDTO(httpStatus, conversation);

        return new ResponseEntity<>(payload, httpStatus);
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<ApiResponseDTO> getConversationWithUser(
            @PathVariable String userEmail,
            HttpServletRequest request) {

        String jwtToken = request.getHeader("Authorization");
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            throw new ApiException("Please provide a valid authorization header!", HttpStatus.BAD_REQUEST);
        }

        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        String userClaimEmail = String.valueOf(claims.get("email", Integer.class));
        ConversationResponseDTO conversation = conversationService.getConversationByMemberEmails(userEmail, userClaimEmail);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponseDTO payload = new ApiSuccessResponseDTO(httpStatus, conversation);

        return new ResponseEntity<>(payload, httpStatus);
    }
}
