package com.fsoft.fintern.websocket;

import com.fsoft.fintern.dtos.NotificationDTO;
import com.fsoft.fintern.enums.NotificationType;
import com.fsoft.fintern.models.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class NotificationSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/notification.sendNotification")
    public void sendNotification(@Payload NotificationDTO dto) {
        NotificationType type = dto.getType();
        Notification notification = new Notification();
        notification.setContent(dto.getContent());
        notification.setUrl(dto.getUrl());
        notification.setNotificationType(type);
        for (Integer recipientId : dto.getRecipientIds()) {
            if(recipientId != dto.getActorId())
                messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, notification);
        }
    }


    @MessageMapping("/notification.getNotification")
    public void getNotification(@Payload Map<String, Object> payload) {
        // This can be used for requesting unseen notifications, etc.
        // Just echoing for now or respond to client if needed
        int userId = (int) payload.get("userId");
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, "Fetching notifications...");
    }
}
