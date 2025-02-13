package com.fsoft.fintern.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.dtos.UserDTO;
import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User.User;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@SessionAttributes("user")
@RequestMapping("authenticate")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public String handleAuthentication(Model model, OAuth2AuthenticationToken token) throws BadRequestException {
        OAuth2User oauth2User = token.getPrincipal();
        String email = oauth2User.getAttribute("email");
        UserDTO userDTO = new UserDTO();
        if(userService.getByEmail(email).getBody() == null) {
            userDTO.setEmail(email);
            userDTO.setFirst_name(oauth2User.getAttribute("given_name"));
            userDTO.setLast_name(oauth2User.getAttribute("family_name"));
            userDTO.setPicture(oauth2User.getAttribute("picture"));
            userDTO.setRole(Role.INTERN);
            RedirectAttributes redirectAttributes;
            model.addAttribute("user", userDTO);
            return "redirect:/profile/edit";
        }
        else {
            User user = userService.getByEmail(email).getBody();
            userDTO.setEmail(email);
            userDTO.setFirst_name(user.getFirst_name());
            userDTO.setLast_name(user.getLast_name());
            userDTO.setPicture(user.getAvatar_path());
            userDTO.setRole(user.getRole());
            model.addAttribute("user", userDTO);
            return "redirect:/profile";
        }
    }
}
