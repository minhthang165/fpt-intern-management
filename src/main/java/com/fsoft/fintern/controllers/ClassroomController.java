package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.ClassroomService;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;

@Controller

@RequestMapping("manage-class")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;


    @GetMapping
    public String redirectManageClassPage(Model model) {
        try{
            ResponseEntity<List<Classroom>> response = classroomService.findAll();
            ResponseEntity<List<User>> response1 = userService.findUserByRole(Role.EMPLOYEE);

            model.addAttribute("classroomList", response.getBody());
            model.addAttribute("userRoleList", response1.getBody());
            return "admin/ManageClass";
        } catch (BadRequestException e) {
            model.addAttribute(ErrorDictionaryConstraints.CLASS_IS_EMPTY.getMessage());
            return "admin/AdminDashboard";
        }

    }
}
