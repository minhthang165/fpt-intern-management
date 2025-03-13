package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.ConversationDTO;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.ConversationRepository;
import com.fsoft.fintern.repositories.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public ResponseEntity<Conversation> createConversation(ConversationDTO conversationDTO) throws BadRequestException {
        Conversation conversation = new Conversation();
        conversation.setConversationName(conversationDTO.getConversation_name());
        conversation.setConversationAvatar(conversationDTO.getConversation_avatar());
        return new ResponseEntity<>(this.conversationRepository.save(conversation), HttpStatus.CREATED);
    }

    public ResponseEntity<Conversation> getConversation(int conversation_id) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversation_id).orElse(null);
        if (conversation == null) {
            throw new BadRequestException("Conversation with id " + conversation_id + " not found");
        }
        else return new ResponseEntity<>(conversation, HttpStatus.OK);
    }

    public ResponseEntity<List<Conversation>> getAllConversations() throws BadRequestException {
        List<Conversation> conversations = this.conversationRepository.findAll();
        if (conversations.isEmpty()) {
            throw new BadRequestException("No conversations found");
        }
        else return new ResponseEntity<>(conversations, HttpStatus.OK);
    }

    public ResponseEntity<Conversation> updateConversation(ConversationDTO conversationDTO) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversationDTO.getConversation_id()).orElse(null);
        if (conversation == null) {
            throw new BadRequestException("Conversation with id " + conversationDTO.getConversation_id() + " not found");
        }
        else {
            conversation.setConversationName(conversationDTO.getConversation_name());
            conversation.setConversationAvatar(conversationDTO.getConversation_avatar());
            return new ResponseEntity<>(this.conversationRepository.save(conversation), HttpStatus.OK);
        }
    }

    //Soft Delete (is_Active false)
    public ResponseEntity<Conversation> deleteConversation(int conversation_id) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversation_id).orElse(null);
        if (conversation == null) {
            throw new BadRequestException("Conversation with id " + conversation_id + " not found");
        }
        else {
            conversation.setActive(false);
            this.conversationRepository.save(conversation);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        }
    }
}
