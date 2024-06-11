package com.vietle.mychatapi.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c " +
        "WHERE c.isGroup = false " +
        "AND SIZE(c.members) = 2 " +
        "AND EXISTS (" +
        "   SELECT 1 FROM ConversationMember cm1 " +
        "   WHERE cm1.conversation = c AND cm1.user.id = :memberId1" +
        ") " +
        " AND EXISTS (" +
        "   SELECT 1 FROM ConversationMember cm2 " +
        "   WHERE cm2.conversation = c AND cm2.user.id = :memberId2" +
        ")")
    Optional<Conversation> findConversationByTwoMembers(@Param("memberId1") Long userId1, @Param("memberId2") Long userId2);

    @Query("SELECT c FROM Conversation c " +
            "JOIN c.members cm " +
            "WHERE cm.user.id = :userId")
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);
}
