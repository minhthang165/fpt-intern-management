package com.fsoft.fintern.controllers;

import com.fsoft.fintern.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("profile")
@SessionAttributes("user")
public class ProfileController {

    @GetMapping("")
    public String profile(Model model) {
        return "profile";
    }

    //Edit profile screen
    @GetMapping("/edit")
    public String getInfo(Model model) {
        return "edit";
    }



}
