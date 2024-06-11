package com.vietle.mychatapi.conversation;

import com.vietle.mychatapi.conversation.dto.ConversationCreateDTO;
import com.vietle.mychatapi.conversation.dto.ConversationResponseDTO;
import com.vietle.mychatapi.message.dto.MessageCreateDTO;
import com.vietle.mychatapi.message.dto.MessageResponseDTO;
import com.vietle.mychatapi.user.dto.UserResponseDTO;
import com.vietle.mychatapi.conversationmember.ConversationMember;
import com.vietle.mychatapi.user.User;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.conversationmember.ConversationMemberRepository;
import com.vietle.mychatapi.user.UserRepository;
import com.vietle.mychatapi.message.MessageService;
import com.vietle.mychatapi.user.UserService;
import com.vietle.mychatapi.websocket.WebSocketSubscriptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Autowired
    private WebSocketSubscriptionHandler subscriptionHandler;

    @Autowired
    private UserService userService;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            ConversationMemberRepository conversationMemberRepository,
            MessageService messageService
    ) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    /**
     * Public method to create new conversation, used by other parts of application
     * @param conversationCreateDTO - the object request sent from client which contains data to create new conversation
     * @return new ConversationResponseDTO object
     */
    public ConversationResponseDTO createConversation(ConversationCreateDTO conversationCreateDTO) {
        ConversationResponseDTO responseDTO;
        if (conversationCreateDTO.getSentTo().size() < 2) {
            UserResponseDTO creator = userService.getUserByEmail(conversationCreateDTO.getCreatorEmail());
            UserResponseDTO receiver = userService.getUserByEmail(conversationCreateDTO.getSentTo().get(0));
            Optional<Conversation> queryConversation = conversationRepository.findConversationByTwoMembers(creator.getUserId(), receiver.getUserId());

            if (queryConversation.isPresent()) {
                responseDTO = createNewMessageForExistingConversation(queryConversation.get(), conversationCreateDTO);
            } else {
                responseDTO = createNewConversation(conversationCreateDTO, false);
            }
        } else {
            responseDTO = createNewConversation(conversationCreateDTO, true);
        }

        return responseDTO;
    }

    public List<ConversationResponseDTO> getAllConversation(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return ConversationResponseDTO.mappedFromConversationList(conversations);
    }

    public ConversationResponseDTO getConversationByMemberEmails(String email1, String email2) {
        ConversationResponseDTO responseDTO = new ConversationResponseDTO();

        Long senderId = userService.getUserByEmail(email1).getUserId();
        Long receiverId = userService.getUserByEmail(email2).getUserId();
        Optional<Conversation> conversation = conversationRepository.findConversationByTwoMembers(senderId, receiverId);

        if (conversation.isPresent()) {
            responseDTO.setConversationId(conversation.get().getConversationId());
            responseDTO.setConversationName(conversation.get().getConversationName());
            responseDTO.setMessages(messageService.getMessagesByConversationId(conversation.get().getConversationId()));
        }

        return responseDTO;
    }

    public ConversationResponseDTO getConversationById(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).get();

        return new ConversationResponseDTO(conversation);
    }

    /**
     * Create new conversation
     * @param isGroup - be true if new message is sent to more than 2 other users
     * @param conversationCreateDTO - the object request sent from client which contains data to create new conversation
     * @return new ConversationResponseDTO object
     */
    private ConversationResponseDTO createNewConversation(ConversationCreateDTO conversationCreateDTO, boolean isGroup) {
        Conversation conversation = Conversation.builder()
                .conversationName(conversationCreateDTO.getConversationName())
                .isGroup(isGroup)
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        UserResponseDTO creator = userService.getUserByEmail(conversationCreateDTO.getCreatorEmail());
        subscriptionHandler.subscribeToConversation(savedConversation.getConversationId(), creator.getUsername());
        for (String memberEmail : conversationCreateDTO.getSentTo()) {
            UserResponseDTO user = userService.getUserByEmail(memberEmail);
            subscriptionHandler.subscribeToConversation(savedConversation.getConversationId(), user.getUsername());
        }

        List<ConversationMember> members = createNewConversationMembers(conversationCreateDTO, savedConversation);
        conversation.setMembers(members);

        return createNewMessageForExistingConversation(savedConversation, conversationCreateDTO);
    }

    /**
     * Create new message for an existing conversation
     * @param conversation - the existing conversation
     * @param conversationCreateDTO - the object request sent from client which contains data to create new conversation
     * @return new ConversationResponseDTO object
     */
    private ConversationResponseDTO createNewMessageForExistingConversation(Conversation conversation, ConversationCreateDTO conversationCreateDTO) {
        ConversationResponseDTO responseDTO = ConversationResponseDTO.builder()
                .conversationId(conversation.getConversationId())
                .conversationName(conversation.getConversationName())
                .build();

        if (conversationCreateDTO.getInitialMessage() != null
                && !conversationCreateDTO.getInitialMessage().isEmpty()
                && conversationCreateDTO.getCreatorEmail() != null) {

            UserResponseDTO user = userService.getUserByEmail(conversationCreateDTO.getCreatorEmail());;
            MessageCreateDTO messageCreateDTO = MessageCreateDTO.builder()
                    .conversationId(conversation.getConversationId())
                    .senderId(user.getUserId())
                    .messageText(conversationCreateDTO.getInitialMessage())
                    .build();

            MessageResponseDTO messageResponseDTO = messageService.createMessage(messageCreateDTO);

            List<MessageResponseDTO> messages = new ArrayList<>();
            messages.add(messageResponseDTO);
            responseDTO.setMessages(messages);
        }

        return responseDTO;
    }

    /**
     * Create a list of members from a provided DTO object
     * @param conversationCreateDTO - the object request sent from client which contains data to create new conversation
     * @return List of new ConversationMembers
     */
    private List<ConversationMember> createNewConversationMembers(ConversationCreateDTO conversationCreateDTO, Conversation conversation) {
        List<String> memberEmailList = conversationCreateDTO.getSentTo();
        memberEmailList.add(conversationCreateDTO.getCreatorEmail());

        List<ConversationMember> members = new ArrayList<>();
        memberEmailList.forEach(
                email -> {
                    Optional<User> queryUser = userRepository.findByEmail(email);
                    if (queryUser.isEmpty()) {
                        throw new ApiException("User not found!", HttpStatus.NO_CONTENT);
                    }

                    ConversationMember newMember = ConversationMember.builder()
                            .user(queryUser.get())
                            .joinedDate(new Date(System.currentTimeMillis()))
                            .conversation(conversation)
                            .build();

                    members.add(conversationMemberRepository.save(newMember));
                }
        );

        return members;
    }
}
