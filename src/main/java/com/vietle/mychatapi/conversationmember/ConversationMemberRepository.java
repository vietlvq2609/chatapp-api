package com.vietle.mychatapi.conversationmember;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
}
