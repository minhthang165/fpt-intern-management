package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

@Controller
@RequestMapping("manage-user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping()
    public String redirectManageUserPage(@SessionAttribute("user") LoginUserDTO user,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         String role,
                                         Model model){
        if (user.getRole() == Role.ADMIN) {
            try {
                Pageable pageable = PageRequest.of(page, size);
                ResponseEntity<Page<User>> users;

                if ("intern".equalsIgnoreCase(role)) {
                    users = userService.findUserByRole(Role.INTERN, pageable);

                    for (User u : Objects.requireNonNull(users.getBody()).getContent()) {
                        if (redisTemplate.opsForValue().get("banned_user:" + u.getId()) != null) {
                            u.setActive(false);
                        }
                    }
                    model.addAttribute("userList", users.getBody());
                    model.addAttribute("selectedRole", role);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("pageSize", size);
                    model.addAttribute("totalPages", users.getBody().getTotalPages());
                    return "Admin/ManageIntern";
                } else if ("employee".equalsIgnoreCase(role)) {
                    users = userService.findUserByRole(Role.EMPLOYEE, pageable);

                    for (User u : Objects.requireNonNull(users.getBody()).getContent()) {
                        if (redisTemplate.opsForValue().get("banned_user:" + u.getId()) != null) {
                            u.setActive(false);
                        }
                    }
                    model.addAttribute("userList", users.getBody());
                    model.addAttribute("selectedRole", role);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("pageSize", size);
                    model.addAttribute("totalPages", users.getBody().getTotalPages());
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
