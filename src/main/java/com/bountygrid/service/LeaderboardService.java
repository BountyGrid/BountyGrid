package com.bountygrid.service;

import com.bountygrid.entity.User;
import com.bountygrid.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private final UserRepository userRepository;

    public List<User> getTopUsers() {
        return userRepository.findTopByPoints(PageRequest.of(0, 20));
    }

    public long getRankFor(User user) {
        return userRepository.countUsersAbove(user.getPoints()) + 1;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyLeaderboard() {
        userRepository.resetMonthlyStats();
    }
}
