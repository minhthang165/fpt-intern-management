package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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

        User existedUser = userService.getByEmail(email).getBody();
        if (existedUser != null) {
            LoginUserDTO loginUserDTO = convertToLoginUserDTO(existedUser);
            model.addAttribute("user", loginUserDTO);
            return "redirect:/" + loginUserDTO.getId();
        }

        // Create new user object
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail(email);
        userDTO.setFirst_name(oauth2User.getAttribute("given_name"));
        userDTO.setLast_name(oauth2User.getAttribute("family_name"));
        userDTO.setPicture(oauth2User.getAttribute("picture"));
        userDTO.setRole(Role.GUEST);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<CreateUserDTO> requestEntity = new HttpEntity<>(userDTO, headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(
                    "http://localhost/api/user/users/create-employee",
                    HttpMethod.POST,
                    requestEntity,
                    User.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                LoginUserDTO loginUserDTO = convertToLoginUserDTO(response.getBody());
                model.addAttribute("user", loginUserDTO);
                return "redirect:/" + loginUserDTO.getId();
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create user.");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error occurred: " + e.getMessage());
            return "redirect:/login";
        }
    }

    private LoginUserDTO convertToLoginUserDTO(User user) {
        LoginUserDTO dto = new LoginUserDTO();
        dto.setId(user.getId());
        dto.setFirst_name(user.getFirst_name());
        dto.setLast_name(user.getLast_name());
        dto.setPicture(user.getAvatar_path());
        dto.setRole(user.getRole());
        return dto;
    }
}
