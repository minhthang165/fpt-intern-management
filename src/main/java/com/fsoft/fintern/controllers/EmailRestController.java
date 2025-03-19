package com.fsoft.fintern.controllers;

import com.fsoft.fintern.models.EmailDetails;
import com.fsoft.fintern.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailRestController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/send-mail")
    public String sendMail(@RequestBody EmailDetails details) {
        return emailService.sendMail(details);
    }
}
