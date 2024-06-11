package com.vietle.mychatapi.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class MessageCreateDTO {
    @NotBlank(message = "Can not find conversation_id in your request!")
    private Long conversationId;

    @NotBlank(message = "Can not find message_text in your request!")
    private String messageText;

    @NotBlank(message = "Can not find sender_id in your request!")
    private Long senderId;
}
