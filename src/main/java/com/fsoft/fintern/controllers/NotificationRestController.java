package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.NotificationDTO;
import com.fsoft.fintern.models.Notification;
import com.fsoft.fintern.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/notification")
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create-notification")
    @Operation(description = "Create new notification")
    public ResponseEntity<Notification> sendNotification(@RequestBody NotificationDTO notificationDTO) throws BadRequestException {
        return this.notificationService.sendNotification(notificationDTO);
    }

    @GetMapping("/get-notification/{user_id}")
    @Operation(description = "Get notification of an user")
    public ResponseEntity<List<Notification>> getNotification(@PathVariable int user_id) {
        return this.notificationService.getNotification(user_id);
    }
}
