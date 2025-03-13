package com.fsoft.fintern.controllers;

import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@SessionAttributes("user")
@RequestMapping("manage-user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public String redirectManageUserPage(Model model) throws BadRequestException {
        User user = (User) model.getAttribute("user");

        if (user.getRole() == Role.ADMIN) {
            return "/admin/ManageUser";
        }
        return "admin/AdminDashboard";
    }


}
