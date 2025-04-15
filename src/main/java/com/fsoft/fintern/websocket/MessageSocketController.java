package com.fsoft.fintern.websocket;

import com.fsoft.fintern.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class MessageSocketController {
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messenger")
    public Message sendMessage(@Payload Message chatMessage) {
        return chatMessage;
    }
    
    @MessageMapping("/chat.userRemoved")
    @SendTo("/topic/messenger")
    public Map<String, Object> userRemoved(@Payload Map<String, Object> payload) {
        return payload;
    }
    
    @MessageMapping("/chat.userAdded")
    @SendTo("/topic/messenger")
    public Map<String, Object> userAdded(@Payload Map<String, Object> payload) {
        return payload;
    }
}
