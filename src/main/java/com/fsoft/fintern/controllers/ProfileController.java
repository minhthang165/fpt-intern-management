package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@SessionAttributes("user")
@Controller
@RequestMapping("profile")
public class ProfileController {

    @GetMapping("")
    public String profile(Model model) {
        return "profile";
    }

    //Edit profile screen
    @GetMapping("/edit")
    public String getInfo(@ModelAttribute("tempUser") CreateUserDTO tempUser, Model model) {
        model.addAttribute("tempUser", tempUser);
        return "edit";
    }
}
