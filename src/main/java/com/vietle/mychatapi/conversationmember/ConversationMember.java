package com.vietle.mychatapi.conversationmember;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vietle.mychatapi.conversation.Conversation;
import com.vietle.mychatapi.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "_conversationMembers")
public class ConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    private Date joinedDate;
    private Date leftDate;
}
