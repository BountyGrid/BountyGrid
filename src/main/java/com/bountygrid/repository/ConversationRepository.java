package com.bountygrid.repository;

import com.bountygrid.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByAlertIdAndPosterIdAndFinderId(Long alertId, Long posterId, Long finderId);

    @Query("select c from Conversation c where c.poster.id = :userId or c.finder.id = :userId order by c.createdAt desc")
    List<Conversation> findForUser(@Param("userId") Long userId);
}
