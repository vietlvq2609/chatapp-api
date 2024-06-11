package com.vietle.mychatapi.conversation.dto;

import com.vietle.mychatapi.conversation.Conversation;
import com.vietle.mychatapi.message.dto.MessageResponseDTO;
import com.vietle.mychatapi.user.dto.UserResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponseDTO {
    private Long conversationId;
    private String conversationName;
    private List<MessageResponseDTO> messages;
    private List<UserResponseDTO> members;

    public ConversationResponseDTO(Conversation conversation) {
        this.conversationId = conversation.getConversationId();
        this.conversationName = conversation.getConversationName();
        this.messages = MessageResponseDTO.mappedFromMessageList(conversation.getMessages());
        this.members = UserResponseDTO.mappedFromConversationMemberList(conversation.getMembers());
    }

    static public List<ConversationResponseDTO> mappedFromConversationList(List<Conversation> conversations) {
        return conversations.stream()
            .map(ConversationResponseDTO::new)
            .collect(Collectors.toList());
    }
}
