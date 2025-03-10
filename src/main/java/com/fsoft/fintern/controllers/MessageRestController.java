package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.MessageDTO;
import com.fsoft.fintern.models.Message;
import com.fsoft.fintern.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/message")
public class MessageRestController {

    private final MessageService messageService;

    @Autowired
    public MessageRestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/get/{conversation_id}")
    @Operation(description = "Get message in a conversation")
    public ResponseEntity<List<Message>> getMessage(@PathVariable int conversation_id) throws BadRequestException {
        return this.messageService.getMessageInConversation(conversation_id);
    }

    @PostMapping("/post")
    @Operation(description = "Post message to a conversation")
    public ResponseEntity<Message> addMessage(@RequestBody MessageDTO messageDTO) throws BadRequestException {
        return this.messageService.addMessage(messageDTO);
    }

    @PatchMapping("/delete/{message_id}")
    @Operation(description = "Delete message from a conversation (soft delete)")
    public ResponseEntity<Message> deleteMessage(@PathVariable int message_id) throws BadRequestException {
        return this.messageService.deleteMessage(message_id);
    }

    @PatchMapping("/update")
    @Operation(description = "Update a message")
    public ResponseEntity<Message> updateMessage(@RequestBody MessageDTO messageDTO) throws BadRequestException {
        return this.messageService.updateMessage(messageDTO);
    }
}
