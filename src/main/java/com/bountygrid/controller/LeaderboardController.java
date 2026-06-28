package com.bountygrid.controller;

import com.bountygrid.entity.User;
import com.bountygrid.service.LeaderboardService;
import com.bountygrid.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeaderboardController extends ControllerSupport {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(UserService userService, LeaderboardService leaderboardService) {
        super(userService);
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("users", leaderboardService.getTopUsers());
        model.addAttribute("rank", leaderboardService.getRankFor(user));
        return "leaderboard";
    }
}
