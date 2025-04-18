package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.ConversationDTO;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.services.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/conversation")
public class ConversationRestController {
    private final ConversationService conversationService;

    @Autowired
    public ConversationRestController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/group/{conversation_id}")
    @Operation(description = "Get group detail")
    public ResponseEntity<Conversation> getGroup(@PathVariable int conversation_id) throws BadRequestException {
        return this.conversationService.getConversation(conversation_id);
    }

    @PostMapping("/group/create")
    @Operation(description = "Create new group")
    public ResponseEntity<Conversation> createGroup(@RequestBody ConversationDTO conversationDTO) throws BadRequestException {
        return this.conversationService.createConversation(conversationDTO);
    }

    @PatchMapping("/group/delete/{conversation_id}")
    @Operation(description = "Delete group")
    public ResponseEntity<Conversation> deleteGroup(@PathVariable int conversation_id) throws BadRequestException {
        return this.conversationService.deleteConversation(conversation_id);
    }

    @PatchMapping("/group/update/{conversation_id}")
    @Operation(description = "Update group")
    public ResponseEntity<Conversation> updateGroup(@PathVariable int conversation_id, @RequestBody ConversationDTO conversationDTO) throws BadRequestException {
        return this.conversationService.updateConversation(conversation_id, conversationDTO);
    }

    @GetMapping("/find-one-to-one/{userId1}/{userId2}")
    @Operation(description = "Find one-to-one conversation between two users")
    public ResponseEntity<Conversation> findOneToOneConversation(@PathVariable int userId1, @PathVariable int userId2) {
        return this.conversationService.findOneToOneConversation(userId1, userId2);
    }
}
