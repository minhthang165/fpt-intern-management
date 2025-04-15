package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Integer> {
}