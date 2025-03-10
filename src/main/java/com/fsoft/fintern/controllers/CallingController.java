package com.fsoft.fintern.controllers;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/calls")
public class CallingController {
    private final SimpMessagingTemplate messagingTemplate;

    public CallingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    @PostMapping("/create/{callerId}/{receiverId}")
    public Map<String, String> createCall(@PathVariable int callerId, @PathVariable int receiverId) {
        String roomID = "room_" + System.currentTimeMillis(); // Tạo roomID duy nhất


        messagingTemplate.convertAndSend("/topic/call/" + receiverId, Map.of(
                "callerId", callerId,
                "roomID", roomID
        ));

        System.out.println("Sent call notification to receiver: " + receiverId); // Log thông báo đã gửi

        return Map.of("roomID", roomID);
    }
}
