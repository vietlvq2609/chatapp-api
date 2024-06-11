package com.vietle.mychatapi.message;

import com.vietle.mychatapi.message.dto.MessageCreateDTO;
import com.vietle.mychatapi.message.dto.MessageResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vietle.mychatapi.response.dto.ApiResponseDTO;
import com.vietle.mychatapi.response.dto.ApiSuccessResponseDTO;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.jwt.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private final MessageService messageService;
    private final JwtTokenUtil jwtTokenUtil;

    public MessageController(MessageService messageService, JwtTokenUtil jwtTokenUtil) {
        this.messageService = messageService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponseDTO> createMessage(
            @PathVariable Long conversationId,
            HttpServletRequest request,
            @Valid @RequestBody MessageCreateDTO messageCreateDTO) {
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            throw new ApiException("Please provide a valid authorization!", HttpStatus.BAD_REQUEST);
        }

        Claims claims = jwtTokenUtil.decodeToken(jwtToken.substring(7));
        String creatorId = String.valueOf(messageCreateDTO.getSenderId());
        String userIdClaim = String.valueOf(claims.get("user_id"));
        if (!userIdClaim.equals(creatorId)) {
            throw new ApiException("Please provide a valid authorization header!", HttpStatus.BAD_REQUEST);
        }

        MessageResponseDTO message = messageService.createMessage(messageCreateDTO);

        HttpStatus httpStatus = HttpStatus.CREATED;
        ApiResponseDTO payload = new ApiSuccessResponseDTO(httpStatus, message);

        return new ResponseEntity<>(payload, httpStatus);
    }

    @MessageMapping("/sendMessage/{conversationId}")
    public void connectWebSocket(@Payload String jsonMessage, @DestinationVariable("conversationId") String conversationId) {
        try {
            // Parse the JSON string into a MessageCreateDTO object
            MessageCreateDTO messageDto = objectMapper.readValue(jsonMessage, MessageCreateDTO.class);

            MessageResponseDTO createdMessage = messageService.sendMessageToConversation(messageDto);

            String jsonResponse = objectMapper.writeValueAsString(createdMessage);

            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeMapping("topic/public")
    public void handleSubscribePublic(@Header("username") String username) {
        messagingTemplate.convertAndSend("/topic/public", "Welcome to public notification!");
    }
}
