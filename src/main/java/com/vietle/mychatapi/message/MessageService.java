package com.vietle.mychatapi.message;

import com.vietle.mychatapi.message.dto.MessageCreateDTO;
import com.vietle.mychatapi.message.dto.MessageResponseDTO;
import com.vietle.mychatapi.conversation.Conversation;
import com.vietle.mychatapi.user.User;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.conversation.ConversationRepository;
import com.vietle.mychatapi.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageReposity messageReposity;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageReposity messageReposity, UserRepository userRepository, ConversationRepository conversationRepository) {
        this.messageReposity = messageReposity;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    public MessageResponseDTO createMessage(MessageCreateDTO messageCreateDTO) {
        Message newMessage = new Message();

        Optional<User> userQuery = userRepository.findById(messageCreateDTO.getSenderId());
        if (userQuery.isPresent()) {
            User sender = userQuery.get();
            newMessage.setSender(sender);
        } else {
            throw new ApiException("Can not find user!", HttpStatus.NO_CONTENT);
        }

        Optional<Conversation> conversationQuery = conversationRepository.findById(messageCreateDTO.getConversationId());
        if (conversationQuery.isPresent()) {
            Conversation conversation = conversationQuery.get();
            newMessage.setConversation(conversation);
        } else {
            throw new ApiException("Can not find conversation!", HttpStatus.NO_CONTENT);
        }

        newMessage.setMessageText(messageCreateDTO.getMessageText());
        newMessage.setSentAt(new Date(System.currentTimeMillis()));
        newMessage.setMessageText(messageCreateDTO.getMessageText());

        Message createdMessage =  messageReposity.save(newMessage);

        MessageResponseDTO responseDTO = new MessageResponseDTO(createdMessage);

        // Broadcast the message to WebSocket subscribers
        String topic = "/topic/conversation/" + messageCreateDTO.getConversationId();
        messagingTemplate.convertAndSend(topic, responseDTO);

        return responseDTO;
    }

    public MessageResponseDTO sendMessageToConversation(MessageCreateDTO messageCreateDTO) {

        Message newMessage = new Message();

        Optional<User> userQuery = userRepository.findById(messageCreateDTO.getSenderId());
        if (userQuery.isPresent()) {
            User sender = userQuery.get();
            newMessage.setSender(sender);
        } else {
            throw new ApiException("Can not find user!", HttpStatus.NO_CONTENT);
        }

        Optional<Conversation> conversationQuery = conversationRepository.findById(messageCreateDTO.getConversationId());
        if (conversationQuery.isPresent()) {
            Conversation conversation = conversationQuery.get();
            newMessage.setConversation(conversation);
        } else {
            throw new ApiException("Can not find conversation!", HttpStatus.NO_CONTENT);
        }

        newMessage.setMessageText(messageCreateDTO.getMessageText());
        newMessage.setSentAt(new Date(System.currentTimeMillis()));
        newMessage.setMessageText(messageCreateDTO.getMessageText());

        Message createdMessage =  messageReposity.save(newMessage);

        return new MessageResponseDTO(createdMessage);
    }

    public List<MessageResponseDTO> getMessagesByConversationId(Long conversationId) {
        List<MessageResponseDTO> responseDTO = new ArrayList<>();

        Optional<Conversation> conversation = conversationRepository.findById(conversationId);

        if (conversation.isEmpty()) throw new ApiException("Can not find conversation", HttpStatus.NO_CONTENT);

        conversation.get().getMessages().forEach(
                message -> {
                    responseDTO.add(new MessageResponseDTO(message));
                }
        );

        return responseDTO;
    }
}
