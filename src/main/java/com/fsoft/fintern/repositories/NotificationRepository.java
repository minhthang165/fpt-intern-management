package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n " +
           "JOIN NotificationRecipient nr ON n.id = nr.notification.id " +
           "WHERE nr.recipient.id = :recipientId " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientId(@Param("recipientId") Integer recipientId);
}