package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    @Query(value = "SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.status != 'DELETED' ORDER BY m.createdAt ASC")
    Optional<List<Message>> findMessagesByConversationId(@Param("conversationId") int conversationId);

}
