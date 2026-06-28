package com.bountygrid.repository;

import com.bountygrid.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Modifying
    @Query("""
            update Message m set m.readByRecipient = true
            where m.conversation.id = :conversationId and m.sender.id <> :readerId
            """)
    void markReadForUser(@Param("conversationId") Long conversationId, @Param("readerId") Long readerId);
}
