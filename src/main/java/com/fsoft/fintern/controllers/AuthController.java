package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
//import com.fsoft.fintern.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes("user")
@RequestMapping("authenticate")
public class AuthController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    @GetMapping
    public String handleAuthentication(Authentication authentication, HttpServletResponse response, Model model, OAuth2AuthenticationToken token) throws BadRequestException {
        OAuth2User oauth2User = token.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // Fetch the existing user
        ResponseEntity<User> existingUserResponse = userService.getByEmail(email);
        User user = (existingUserResponse != null && existingUserResponse.getBody() != null)
                ? existingUserResponse.getBody()
                : createNewUser(oauth2User);

        if (user == null) {
            return "redirect:/login"; // User creation failed, redirect to login
        }

        if (redisTemplate.opsForValue().get("banned_user:"+user.getId()) != null) {
            return "redirect:/logout";
        }

        // Convert user to DTO and store in session
        LoginUserDTO loginUserDTO = convertToLoginUserDTO(user);
        model.addAttribute("user", loginUserDTO);

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.EMPLOYEE || user.getRole() == Role.INTERN) {
            return "redirect:/manage-user";
        }
        return "redirect:/home";
    }

    private User createNewUser(OAuth2User oauth2User) {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail(oauth2User.getAttribute("email"));
        userDTO.setFirst_name(oauth2User.getAttribute("given_name"));
        userDTO.setLast_name(oauth2User.getAttribute("family_name"));
        userDTO.setPicture(oauth2User.getAttribute("picture"));
        userDTO.setRole(Role.GUEST);

        try {
            ResponseEntity<User> response = userService.createEmployeeOrGuest(userDTO);
            return response != null && response.getBody() != null ? response.getBody() : null;
        } catch (Exception e) {
            e.printStackTrace(); // Log error for debugging
            return null;
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