package com.fsoft.fintern.controllers;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("{user_id}")
public class ProfileController {

    @GetMapping("")
    public String profile(@PathVariable int user_id, Model model) throws BadRequestException {
        model.addAttribute("user_id", user_id);
        return "profile";
    }

}
