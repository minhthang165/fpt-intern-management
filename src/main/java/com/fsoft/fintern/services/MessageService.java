package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.MessageDTO;
import com.fsoft.fintern.enums.MessageStatus;
import com.fsoft.fintern.models.Message;
import com.fsoft.fintern.repositories.ConversationRepository;
import com.fsoft.fintern.repositories.MessageRepository;
import com.fsoft.fintern.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageService(final MessageRepository messageRepository, final ConversationRepository conversationRepository, final UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<List<Message>> getMessageInConversation(int conversation_id) {
        List<Message> messages = this.messageRepository.findMessagesByConversationId(conversation_id).orElse(null);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    public ResponseEntity<Message> addMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setMessageContent(messageDTO.getMessageContent());
        message.setMessageType(messageDTO.getMessageType());
        message.setSender(userRepository.findById(messageDTO.getSenderId()).orElse(null));
        message.setConversation(conversationRepository.findById(messageDTO.getConversationId()).orElse(null));
        message.setStatus(MessageStatus.SENT);
        return new ResponseEntity<>(messageRepository.save(message), HttpStatus.CREATED);
    }

    public ResponseEntity<Message> updateMessage(MessageDTO messageDTO) {
        Message message = this.messageRepository.findById(messageDTO.getMessageId()).orElse(null);
        message.setMessageContent(messageDTO.getMessageContent());
        return new ResponseEntity<>(messageRepository.save(message), HttpStatus.OK);
    }

    public ResponseEntity<Message> deleteMessage(int message_id) {
        Message message = this.messageRepository.findById(message_id).orElse(null);
        message.setStatus(MessageStatus.DELETED);
        return new ResponseEntity<>(messageRepository.save(message), HttpStatus.OK);
    }

}
