package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("messenger")
public class MessengerController {
    @GetMapping("")
    public String messenger(@SessionAttribute("user") LoginUserDTO user, Model model) {
        model.addAttribute("user", user);
        return "messenger";
    }
}
