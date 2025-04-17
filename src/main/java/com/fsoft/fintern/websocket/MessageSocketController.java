package com.fsoft.fintern.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Map;

@Controller
public class MessageSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageSocketController(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> payload) {
        Message message = objectMapper.convertValue(payload.get("message"), Message.class);
        List<Integer> recipients = (List<Integer>) payload.get("recipients");

        // Gửi tin nhắn đến từng người nhận
        for (Integer recipientId : recipients) {
            messagingTemplate.convertAndSend(
                    "/topic/messenger/" + recipientId,
                    message
            );
        }
    }

    @MessageMapping("/chat.userRemoved")
    public void userRemoved(@Payload Map<String, Object> payload) {
        List<Integer> recipients = (List<Integer>) payload.get("recipients");
        for (Integer recipientId : recipients) {
            messagingTemplate.convertAndSend(
                    "/topic/messenger/" + recipientId,
                    payload
            );
        }
    }

    @MessageMapping("/chat.userAdded")
    public void userAdded(@Payload Map<String, Object> payload) {
        List<Integer> recipients = (List<Integer>) payload.get("recipients");
        for (Integer recipientId : recipients) {
            messagingTemplate.convertAndSend(
                    "/topic/messenger/" + recipientId,
                    payload
            );
        }
    }
}
