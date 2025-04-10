package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.EmbedableID.ConversationUserId;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.models.ConversationUser;
import com.fsoft.fintern.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationUserRepository extends JpaRepository<ConversationUser, ConversationUserId> {
    @Query("SELECT cu FROM ConversationUser cu WHERE cu.conversation.id = :conversationId")
    Optional<List<ConversationUser>> findUsersByConversationId(@Param("conversationId") int conversationId);

    @Query("SELECT co.conversation FROM ConversationUser co WHERE co.user.id = :user_id")
    Optional<List<Conversation>> findConversationOfAUser(@Param("user_id") int user_id);

    @Query("SELECT cu.user FROM ConversationUser cu WHERE cu.id = :conversationId")
    Optional<Conversation> findAUserInConversation(int conversationId, int userId);

    @Query("SELECT cu.user FROM ConversationUser cu WHERE cu.conversation.id = :conversationId AND cu.user.id != :userId")
    Optional<User> findOtherUserInOneToOneConversation(@Param("conversationId") int conversationId, @Param("userId") int userId);

    @Query("SELECT c FROM Conversation c WHERE c.type = 'OneToOne' AND c.id IN " +
           "(SELECT cu1.conversation.id FROM ConversationUser cu1 WHERE cu1.user.id = :userId1) AND c.id IN " +
           "(SELECT cu2.conversation.id FROM ConversationUser cu2 WHERE cu2.user.id = :userId2)")
    Optional<Conversation> findOneToOneConversationBetweenUsers(@Param("userId1") int userId1, @Param("userId2") int userId2);
}
