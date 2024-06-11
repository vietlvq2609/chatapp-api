package com.vietle.mychatapi.message.dto;

import com.vietle.mychatapi.user.dto.UserResponseDTO;
import com.vietle.mychatapi.message.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponseDTO {
    private Long messageId;
    private String messageText;
    private Date sentAt;
    private UserResponseDTO sender;

    public MessageResponseDTO(Message message) {
        this.messageId = message.getMessageId();
        this.messageText = message.getMessageText();
        this.sentAt = message.getSentAt();
        this.sender = new UserResponseDTO(message.getSender());
    }

    static public List<MessageResponseDTO> mappedFromMessageList(List<Message> messages) {
        return messages.stream()
            .map(MessageResponseDTO::new)
            .collect(Collectors.toList());
    }
}
