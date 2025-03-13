package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@SessionAttributes("user")
@RequestMapping("authenticate")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public String handleAuthentication(Model model, RedirectAttributes redirectAttributes, OAuth2AuthenticationToken token) throws BadRequestException {
        OAuth2User oauth2User = token.getPrincipal();
        String email = oauth2User.getAttribute("email");
        CreateUserDTO userDTO = new CreateUserDTO();

        User existedUser = userService.getByEmail(email).getBody();

        if(existedUser == null) {
            userDTO.setEmail(email);
            userDTO.setFirst_name(oauth2User.getAttribute("given_name"));
            userDTO.setLast_name(oauth2User.getAttribute("family_name"));
            userDTO.setPicture(oauth2User.getAttribute("picture"));
            userDTO.setRole(Role.INTERN);
            model.addAttribute("user", userDTO);

            return "redirect:/profile/edit";
        }
        model.addAttribute("user", existedUser);
        return "admin/AdminDashboard";
    }
}
