package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.ConversationDTO;
import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.ConversationRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.repositories.ConversationUserRepository;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    @Autowired
    private ConversationUserRepository conversationUserRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public ResponseEntity<Conversation> createConversation(ConversationDTO conversationDTO) throws BadRequestException {
        Conversation conversation = new Conversation();
        conversation.setConversationName(conversationDTO.getConversation_name());
        conversation.setConversationAvatar(conversationDTO.getConversation_avatar());
        conversation.setType(conversationDTO.getType());
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

    public ResponseEntity<Conversation> updateConversation(int conversation_id, ConversationDTO conversationDTO) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversation_id).orElse(null);

        if (conversation == null) {
            throw new BadRequestException("Conversation with id " + conversation_id + " not found");
        }

        Optional.ofNullable(conversationDTO.getConversation_name()).ifPresent(conversation::setConversationName);
        Optional.ofNullable(conversationDTO.getConversation_avatar()).ifPresent(conversation::setConversationAvatar);
        Optional.of(conversationDTO.isIs_Active()).ifPresent(conversation::setActive);

        return new ResponseEntity<>(this.conversationRepository.save(conversation), HttpStatus.OK);
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

    public ResponseEntity<Conversation> findOneToOneConversation(int userId1, int userId2) {
        Conversation conversation = this.conversationUserRepository.findOneToOneConversationBetweenUsers(userId1, userId2).orElse(null);
        if (conversation == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } else {
            // Nếu đây là cuộc trò chuyện one-to-one, thì thêm thông tin người dùng khác vào tên cuộc trò chuyện
            User otherUser = this.conversationUserRepository.findOtherUserInOneToOneConversation(conversation.getId(), userId1).orElse(null);
            if (otherUser != null) {
                conversation.setConversationName(otherUser.getFirst_name() + " " + otherUser.getLast_name());
                conversation.setConversationAvatar(otherUser.getAvatar_path());
            }
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        }
    }
}
