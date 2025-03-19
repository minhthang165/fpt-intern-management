package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("manage-user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping()
    public String redirectManageUserPage(@SessionAttribute("user") LoginUserDTO user, String role, Model model){


        if (user.getRole() == Role.ADMIN) {
            try {
                ResponseEntity<List<User>> users = userService.findAll();
                for (User u : Objects.requireNonNull(users.getBody())) {
                    if (redisTemplate.opsForValue().get("banned_user:" + u.getId()) != null) {
                        u.setActive(false);
                    }
                }
                model.addAttribute("userList", users.getBody());
                model.addAttribute("selectedRole", role);

                if ("intern".equalsIgnoreCase(role)) {
                    return "Admin/ManageIntern";
                } else if ("employee".equalsIgnoreCase(role)) {
                    return "Admin/ManageEmployee";
                }
                return "Admin/AdminDashboard";
            } catch (BadRequestException e) {
                model.addAttribute("errorMessage", ErrorDictionaryConstraints.USERS_IS_EMPTY.getMessage());
            }
        }
        return "admin/AdminDashboard";
    }
  
    @GetMapping("/current-user-id")
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return userId;
    }
}
