package com.fsoft.fintern.controllers;

import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
@Controller
@RequestMapping("/users")
public class UserListController {
    private final UserService userService;

    @Autowired
    public UserListController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{user_id}")
    public String listUsers(@PathVariable("user_id") int userId, Model model) throws BadRequestException {
        ResponseEntity<List<User>> response = userService.findAll();

        model.addAttribute("user_id", userId);
        if (response.getBody() != null) {
            model.addAttribute("users", response.getBody());
        } else {
            model.addAttribute("users", List.of());
        }

        return "user-list";
    }
}