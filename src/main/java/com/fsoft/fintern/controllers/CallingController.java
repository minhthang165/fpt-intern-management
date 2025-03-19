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

    @PostMapping("/create/{callerId}/{receiverId}/{roomID}")
    public Map<String, String> createCall(@PathVariable int callerId, @PathVariable int receiverId, @PathVariable int roomID) {
        System.out.println("Creating call: Caller " + callerId + " -> Receiver " + receiverId + " in Room " + roomID);

        messagingTemplate.convertAndSend("/topic/call/" + receiverId, Map.of(
                "callerId", String.valueOf(callerId),
                "roomID", String.valueOf(roomID)
        ));

        System.out.println("✅ Sent call notification to receiver: " + receiverId);
        return Map.of("roomID", String.valueOf(roomID));
    }
}
