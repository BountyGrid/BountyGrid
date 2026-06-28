package com.bountygrid.controller;

import com.bountygrid.dto.AlertRequest;
import com.bountygrid.dto.AlertResponse;
import com.bountygrid.dto.LoginRequest;
import com.bountygrid.dto.MessageRequest;
import com.bountygrid.dto.RegisterRequest;
import com.bountygrid.dto.StoryRequest;
import com.bountygrid.entity.Conversation;
import com.bountygrid.entity.Message;
import com.bountygrid.entity.RecoveryStory;
import com.bountygrid.entity.SosBroadcast;
import com.bountygrid.entity.Transaction;
import com.bountygrid.entity.User;
import com.bountygrid.service.AlertService;
import com.bountygrid.service.AuthService;
import com.bountygrid.service.LeaderboardService;
import com.bountygrid.service.MessageService;
import com.bountygrid.service.SosService;
import com.bountygrid.service.StoryService;
import com.bountygrid.service.UserService;
import com.bountygrid.service.WalletService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final AuthService authService;
    private final UserService userService;
    private final AlertService alertService;
    private final MessageService messageService;
    private final WalletService walletService;
    private final LeaderboardService leaderboardService;
    private final SosService sosService;
    private final StoryService storyService;

    public ApiController(AuthService authService, UserService userService, AlertService alertService,
                         MessageService messageService, WalletService walletService,
                         LeaderboardService leaderboardService, SosService sosService, StoryService storyService) {
        this.authService = authService;
        this.userService = userService;
        this.alertService = alertService;
        this.messageService = messageService;
        this.walletService = walletService;
        this.leaderboardService = leaderboardService;
        this.sosService = sosService;
        this.storyService = storyService;
    }

    @PostMapping("/auth/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        return Map.of("token", authService.register(request));
    }

    @PostMapping("/auth/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        return Map.of("token", authService.login(request.email(), request.password()));
    }

    @GetMapping("/auth/me")
    public User me(Principal principal) {
        return current(principal);
    }

    @PostMapping("/auth/refresh")
    public Map<String, String> refresh(Principal principal) {
        return Map.of("token", authService.refreshToken(principal.getName()));
    }

    @GetMapping("/alerts/nearby")
    public List<AlertResponse> nearby(@RequestParam double lat, @RequestParam double lng,
                                      @RequestParam(defaultValue = "10") double radius) {
        return alertService.findNearby(lat, lng, radius);
    }

    @PostMapping("/alerts")
    public Map<String, Long> createAlert(@Valid @RequestBody AlertRequest request, Principal principal) {
        return Map.of("id", alertService.create(current(principal), request, null).getId());
    }

    @PostMapping("/messages/conversations")
    public Conversation startConversation(@RequestParam Long alertId, Principal principal) {
        return messageService.startConversation(current(principal), alertId);
    }

    @GetMapping("/messages/conversations/{id}")
    public List<Message> messages(@PathVariable Long id, Principal principal) {
        return messageService.getMessages(current(principal), id);
    }

    @PostMapping("/messages/conversations/{id}/send")
    public Message sendMessage(@PathVariable Long id, @Valid @RequestBody MessageRequest request, Principal principal) {
        return messageService.sendMessage(current(principal), id, request.content());
    }

    @GetMapping("/wallet")
    public Map<String, Double> wallet(Principal principal) {
        return Map.of("balance", current(principal).getWalletBalance());
    }

    @GetMapping("/wallet/transactions")
    public List<Transaction> transactions(Principal principal) {
        return walletService.getTransactions(current(principal));
    }

    @PostMapping("/wallet/deposit")
    public Map<String, String> deposit(@RequestParam double amount, Principal principal) {
        walletService.deposit(current(principal), amount);
        return Map.of("status", "ok");
    }

    @PostMapping("/wallet/withdraw")
    public Map<String, String> withdraw(@RequestParam double amount, Principal principal) {
        walletService.withdraw(current(principal), amount);
        return Map.of("status", "ok");
    }

    @GetMapping("/leaderboard")
    public List<User> leaderboard() {
        return leaderboardService.getTopUsers();
    }

    @GetMapping("/leaderboard/my-rank")
    public Map<String, Number> myRank(Principal principal) {
        User user = current(principal);
        return Map.of("rank", leaderboardService.getRankFor(user), "points", user.getPoints(), "finds", user.getFinds());
    }

    @PostMapping("/sos/broadcast")
    public SosBroadcast broadcast(@RequestParam Long alertId, @RequestParam(defaultValue = "25") double radiusKm,
                                  Principal principal) {
        return sosService.broadcast(current(principal), alertId, radiusKm);
    }

    @GetMapping("/sos/active")
    public List<SosBroadcast> activeSos() {
        return sosService.getActive();
    }

    @GetMapping("/stories")
    public List<RecoveryStory> stories() {
        return storyService.getPublicStories();
    }

    @PostMapping("/stories")
    public RecoveryStory createStory(@Valid @RequestBody StoryRequest request, Principal principal) {
        return storyService.create(current(principal), request);
    }

    @PostMapping("/stories/{id}/react")
    public RecoveryStory react(@PathVariable Long id, @RequestParam(defaultValue = "heart") String reaction) {
        return storyService.react(id, reaction);
    }

    private User current(Principal principal) {
        return userService.getByEmail(principal.getName());
    }
}
