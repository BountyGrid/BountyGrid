package com.bountygrid.repository;

import com.bountygrid.entity.RecoveryStory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoveryStoryRepository extends JpaRepository<RecoveryStory, Long> {
    List<RecoveryStory> findByPublicStoryTrueOrderByCreatedAtDesc();
}
