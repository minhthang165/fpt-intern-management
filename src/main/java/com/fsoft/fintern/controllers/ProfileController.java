package com.fsoft.fintern.controllers;
import com.fsoft.fintern.dtos.LoginUserDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("")
    public String profile(@SessionAttribute("user") LoginUserDTO loginUserDTO, @RequestParam(name = "user_id", required = false) Integer user_id, Model model) throws BadRequestException {
        if (user_id != null) {
            model.addAttribute("user", loginUserDTO);
        }
        else {
            model.addAttribute("user", loginUserDTO);
        }
        return "profile";
    }

}