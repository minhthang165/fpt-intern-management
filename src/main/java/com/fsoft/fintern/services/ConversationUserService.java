package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.ConversationDTO;
import com.fsoft.fintern.dtos.ConversationUserDTO;
import com.fsoft.fintern.models.CompositeKey.ConversationUserId;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.models.ConversationUser;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ConversationRepository;
import com.fsoft.fintern.repositories.ConversationUserRepository;
import com.fsoft.fintern.repositories.UserRepository;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationUserService {

    @Autowired
    private final ConversationUserRepository conversationUserRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ConversationRepository conversationRepository;

    public ConversationUserService(ConversationUserRepository conversationUserRepository, UserRepository userRepository, ConversationRepository conversationRepository) {
        this.conversationUserRepository = conversationUserRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    public ResponseEntity<ConversationUser> addUserToConversation(ConversationUserDTO conversationUserDTO) {
        User user = userRepository.findById(conversationUserDTO.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepository.findById(conversationUserDTO.getConversation_id())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Create ConversationUser entity
        ConversationUserId id = new ConversationUserId(conversationUserDTO.getUser_id(), conversationUserDTO.getConversation_id());
        ConversationUser conversationUser = new ConversationUser();
        conversationUser.setId(id);
        conversationUser.setUser(user);
        conversationUser.setConversation(conversation);
        conversationUser.setAdmin(false);

        ConversationUser savedUser = conversationUserRepository.save(conversationUser);

        return new ResponseEntity<>(savedUser,HttpStatus.OK);
    }

    public ResponseEntity<List<User>> getAllUserFromConversation(int conversationId) {
        List<User> users = new ArrayList<>();
        users = this.conversationUserRepository.findUsersByConversationId(conversationId).orElse(null);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    public ResponseEntity<List<Conversation>> getAllConversationsByUserId(int user_id) {
        List<Conversation> conversations = new ArrayList<>();
        conversations = this.conversationUserRepository.findConversationOfAUser(user_id).orElse(null);
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }

    public ResponseEntity<ConversationUser> removeAUserFromConversation(int conversationId, int user_id) {
        ConversationUserId id = new ConversationUserId(conversationId, user_id);
        ConversationUser conversationUser = this.conversationUserRepository.findById(id).orElse(null);
        if (conversationUser != null) {
            this.conversationUserRepository.delete(conversationUser);
        } else throw new RuntimeException("User not found");
        return new ResponseEntity<>(conversationUser, HttpStatus.OK);
    }
}
