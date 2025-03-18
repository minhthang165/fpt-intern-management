package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CompositeKey.ConversationUserId;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.models.ConversationUser;
import com.fsoft.fintern.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationUserRepository extends JpaRepository<ConversationUser, ConversationUserId> {
    @Query("SELECT cu.user FROM ConversationUser cu WHERE cu.conversation.id = :conversationId")
    Optional<List<User>> findUsersByConversationId(@Param("conversationId") int conversationId);

    @Query("SELECT co.conversation FROM ConversationUser co WHERE co.user.id = :user_id")
    Optional<List<Conversation>> findConversationOfAUser(@Param("user_id") int user_id);

    @Query("SELECT cu.user FROM ConversationUser cu WHERE cu.id = :conversationId")
    Optional<Conversation> findAUserInConversation(int conversationId, int userId);
}
