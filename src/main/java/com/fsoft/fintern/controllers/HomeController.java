package com.fsoft.fintern.controllers;

import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("home")
public class HomeController {
    @GetMapping("/{user_id}")
    public String home(@PathVariable("user_id") Integer userId, Model model) {
        if (userId == null) {
            model.addAttribute("user_id", "Unknown");
        } else {
            model.addAttribute("user_id", userId);
        }
        return "home";
    }
}


