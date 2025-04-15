package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("home")
public class HomeController {
    @GetMapping("")
    public String home(@SessionAttribute("user") LoginUserDTO loginUserDTO, Model model) {
        if (loginUserDTO != null) {
            model.addAttribute("user", loginUserDTO);
            return "landing-page";
        }
        else {
            return "redirect:/logout";
        }
    }
}



