package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.ui.Model;

@Controller
@RequestMapping("edit")
public class EditProfileController {
    @GetMapping("")
    public String getInfo(@SessionAttribute("user") LoginUserDTO loginUserDTO, Model model) {
        model.addAttribute("user", loginUserDTO);
        return "edit";
    }
}
