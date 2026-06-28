package com.bountygrid.service;

import com.bountygrid.entity.Alert;
import com.bountygrid.entity.Conversation;
import com.bountygrid.entity.Message;
import com.bountygrid.entity.User;
import com.bountygrid.repository.ConversationRepository;
import com.bountygrid.repository.MessageRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Conversation startConversation(User finder, Long alertId) {
        Alert alert = alertService.getById(alertId);
        return conversationRepository.findByAlertIdAndPosterIdAndFinderId(alertId, alert.getOwner().getId(), finder.getId())
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .alert(alert)
                        .poster(alert.getOwner())
                        .finder(finder)
                        .build()));
    }

    public List<Conversation> getConversations(User user) {
        return conversationRepository.findForUser(user.getId());
    }

    @Transactional
    public List<Message> getMessages(User user, Long conversationId) {
        messageRepository.markReadForUser(conversationId, user.getId());
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message sendMessage(User sender, Long conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, message);
        return message;
    }
}
