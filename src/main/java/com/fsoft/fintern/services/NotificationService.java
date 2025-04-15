package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.NotificationDTO;
import com.fsoft.fintern.enums.NotificationType;
import com.fsoft.fintern.models.Notification;
import com.fsoft.fintern.models.NotificationRecipient;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.NotificationRecipientRepository;
import com.fsoft.fintern.repositories.NotificationRepository;
import com.fsoft.fintern.repositories.UserRepository;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRecipientRepository notificationRecipientRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public ResponseEntity<Notification> sendNotification(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        User user = userRepository.findById(notificationDTO.getActorId()).orElse(null);
        notification.setActor(user);
        notification.setNotificationType(notificationDTO.getType());
        notification.setContent(notificationDTO.getContent());
        notification.setUrl(notificationDTO.getUrl());
        notificationRepository.save(notification);

        for (Integer recipientId : notificationDTO.getRecipientIds()) {
            User receiver = userRepository.findById(recipientId).orElse(null);
            NotificationRecipient recipient = new NotificationRecipient(notification, receiver);
            notificationRecipientRepository.save(recipient);
        }
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    public ResponseEntity<List<Notification>> getNotification(int userId) {
        return new ResponseEntity<>(notificationRepository.findByRecipientId(userId), HttpStatus.OK);
    }
}
