package com.bountygrid.service;

import com.bountygrid.dto.StoryRequest;
import com.bountygrid.entity.Alert;
import com.bountygrid.entity.RecoveryStory;
import com.bountygrid.entity.User;
import com.bountygrid.repository.RecoveryStoryRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryService {
    private final RecoveryStoryRepository recoveryStoryRepository;
    private final AlertService alertService;

    public List<RecoveryStory> getPublicStories() {
        return recoveryStoryRepository.findByPublicStoryTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public RecoveryStory create(User author, StoryRequest request) {
        Alert alert = alertService.getById(request.alertId());
        return recoveryStoryRepository.save(RecoveryStory.builder()
                .alert(alert)
                .author(author)
                .title(request.title())
                .story(request.story())
                .recoveryTimeHours(request.recoveryTimeHours())
                .publicStory(request.publicStory() == null || request.publicStory())
                .build());
    }

    @Transactional
    public RecoveryStory react(Long id, String reaction) {
        RecoveryStory story = recoveryStoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));
        if ("clap".equalsIgnoreCase(reaction)) {
            story.setClaps(story.getClaps() + 1);
        } else {
            story.setHearts(story.getHearts() + 1);
        }
        return recoveryStoryRepository.save(story);
    }
}
