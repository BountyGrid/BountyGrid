package com.bountygrid.service;

import com.bountygrid.entity.Notification;
import com.bountygrid.entity.User;
import com.bountygrid.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void notify(User user, String title, String body, String targetUrl) {
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .targetUrl(targetUrl)
                .build());
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", notification);
    }
}
