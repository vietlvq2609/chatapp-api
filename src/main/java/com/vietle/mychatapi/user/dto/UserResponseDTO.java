package com.vietle.mychatapi.user.dto;

import com.vietle.mychatapi.conversationmember.ConversationMember;
import com.vietle.mychatapi.user.User;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserResponseDTO {
    private Long userId;

    private String username;

    private String email;

    private String phoneNumber;

    private String dateOfBirth;

    private String profilePhotoUrl;

    public UserResponseDTO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.dateOfBirth = dateFormat.format(user.getDateOfBirth());
        this.profilePhotoUrl = user.getProfilePhotoUrl();
    }

    static public List<UserResponseDTO> mappedFromUserList(List<User> users) {
        return users.stream()
            .map(UserResponseDTO::new)
            .collect(Collectors.toList());
    }

    static public List<User> getUsersFromConverstionMemberList(List<ConversationMember> members) {
        return members.stream()
            .map(ConversationMember::getUser)
            .collect(Collectors.toList());
    }

    static public List<UserResponseDTO> mappedFromConversationMemberList(List<ConversationMember> members) {
        return mappedFromUserList(getUsersFromConverstionMemberList(members));
    }
}
