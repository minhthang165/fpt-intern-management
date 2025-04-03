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
                                         @RequestParam(name="role", required=false) String role,
                                         Model model){
        model.addAttribute("user", user);
        if (user.getRole() == Role.ADMIN) {
            if ("intern".equals(role)) {
                model.addAttribute("selectedRole", role);
                return "Admin/ManageIntern";
            } else if ("employee".equals(role)) {
                model.addAttribute("selectedRole", role);
                return "Admin/ManageEmployee";
            }
            return "Admin/AdminDashboard";
        }

        if (user.getRole() == Role.EMPLOYEE) {
            return "employee/EmployeeDashboard";
        }

        if (user.getRole() == Role.INTERN) {
            return "intern/InternDashboard";
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
