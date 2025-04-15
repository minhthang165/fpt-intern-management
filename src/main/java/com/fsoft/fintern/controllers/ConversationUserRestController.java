package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.ConversationUserDTO;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.models.ConversationUser;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.ConversationService;
import com.fsoft.fintern.services.ConversationUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/conversation-user/")
public class ConversationUserRestController {

    @Autowired
    private ConversationUserService conversationUserService;

    @GetMapping("/conversation/{user_id}")
    @Operation(description = "Get conversation of a user")
    public ResponseEntity<List<Conversation>> getAllConversation(@PathVariable int user_id) throws BadRequestException {
        return this.conversationUserService.getAllConversationsByUserId(user_id);
    }

    @GetMapping("/get-users/{conversation_id}")
    @Operation(description = "Get all users in a conversation")
    public ResponseEntity<List<ConversationUser>> getAllUsersInConversation(@PathVariable int conversation_id) throws BadRequestException {
        return this.conversationUserService.getAllUserFromConversation(conversation_id);
    }

    @PostMapping("/add-user")
    @Operation(description = "Add user to group")
    public ResponseEntity<ConversationUser> addUserToGroup(@RequestBody ConversationUserDTO conversationUserDTO) throws BadRequestException {
        return this.conversationUserService.addUserToConversation(conversationUserDTO);
    }

    @DeleteMapping("/{conversationId}/users/{userId}")
    public ResponseEntity<String> removeUser(@PathVariable int conversationId, @PathVariable int userId) {
            return this.conversationUserService.removeAUserFromConversation(conversationId, userId);
    }

}
