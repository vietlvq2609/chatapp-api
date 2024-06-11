package com.vietle.mychatapi.conversation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConversationCreateDTO {

    private String conversationName;

    private String initialMessage;

    @NotNull(message = "Creator Id is required to create new conversation!")
    private String creatorEmail;

    @NotNull(message = "Can not find receiver Ids in your request!")
    private List<String> sentTo;
}
