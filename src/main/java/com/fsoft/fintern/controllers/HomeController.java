package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("home")
public class HomeController {
    @GetMapping("")
    public String home(@SessionAttribute("user") LoginUserDTO loginUserDTO, @RequestParam(name = "user_id", required = false) Integer user_id, Model model) {
        if (user_id != null) {
            model.addAttribute("user_id", user_id);
            return "home";
        }
        else {
            model.addAttribute("user_id", loginUserDTO.getId());
            return "home";
        }
    }
}



