package com.vietle.mychatapi.conversation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vietle.mychatapi.conversationmember.ConversationMember;
import com.vietle.mychatapi.message.Message;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "_conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    @Column
    private String conversationName;

    @Column
    private boolean isGroup;

    @JsonIgnore
    @OneToMany(mappedBy = "conversation")
    private List<Message> messages;

    @OneToMany(mappedBy = "conversation")
    private List<ConversationMember> members;
}
