package com.fsoft.fintern.controllers;
import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("profile")
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{user_id}")
    public String profile(@PathVariable int user_id, Model model) throws BadRequestException {
        model.addAttribute("user_id", user_id);
            return "profile";
    }


    @GetMapping("/edit")
    public String getInfo(@ModelAttribute("tempUser") CreateUserDTO tempUser, Model model) {
        model.addAttribute("tempUser", tempUser);
        return "edit";
    }

}
