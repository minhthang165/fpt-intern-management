package com.fsoft.fintern.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
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
        User user = userService.getByEmail(email).getBody();
        if(user == null) {
            CreateUserDTO createUserDTO = new CreateUserDTO();
            createUserDTO.setEmail(email);
            createUserDTO.setFirst_name(oauth2User.getAttribute("given_name"));
            createUserDTO.setLast_name(oauth2User.getAttribute("family_name"));
            createUserDTO.setPicture(oauth2User.getAttribute("picture"));
            createUserDTO.setRole(Role.INTERN);
            redirectAttributes.addFlashAttribute("tempUser", createUserDTO);
            return "redirect:/profile/edit";
        }
        else {
            LoginUserDTO loginUserDTO = new LoginUserDTO();
            loginUserDTO.setFirst_name(user.getFirst_name());
            loginUserDTO.setLast_name(user.getLast_name());
            loginUserDTO.setPicture(user.getAvatar_path());
            loginUserDTO.setRole(user.getRole());
            model.addAttribute("user", loginUserDTO);
            return "redirect:/profile/";
        }
    }
}
