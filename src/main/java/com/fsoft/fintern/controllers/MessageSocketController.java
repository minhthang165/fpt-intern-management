package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.MessageDTO;
import com.fsoft.fintern.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageSocketController {
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messenger")
    public Message sendMessage(@Payload Message chatMessage) {
        return chatMessage;
    }
}
