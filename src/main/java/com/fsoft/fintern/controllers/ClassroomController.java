package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.ClassroomService;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;


import java.util.List;

@Controller

@RequestMapping("manage-class")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;


    @GetMapping
    public String redirectManageClassPage(@SessionAttribute("user") LoginUserDTO user, Model model,
                                          @RequestParam(name= "role", required = false) String role, Pageable pageable) {
        model.addAttribute("user", user);
        if (user.getRole() == Role.ADMIN) {
            try {
                ResponseEntity<Page<User>> response = userService.findUserByRole(Role.EMPLOYEE, pageable);
                model.addAttribute("userRoleList", response.getBody());
                return "admin/ManageClass";
            } catch (BadRequestException e) {
                model.addAttribute("error", ErrorDictionaryConstraints.CLASS_IS_EMPTY.getMessage());
                return "admin/AdminDashboard";
            }
        } else if (user.getRole() == Role.EMPLOYEE) {
            try {
                ResponseEntity<Page<User>> response = userService.findUserByRole(Role.INTERN, pageable);
                model.addAttribute("internClassList", response.getBody());
                List<Classroom> classrooms = classroomService.getClassroomsByMentorId(user.getId());
                model.addAttribute("classList", classrooms);
                model.addAttribute("mentorId", user.getId());
                return "employee/EmpManageClass";

            } catch (BadRequestException e) {
                model.addAttribute("error", ErrorDictionaryConstraints.CLASS_IS_EMPTY.getMessage());
                return "employee/EmployeeDashboard";
            }
        } else {
            return "errorPage";
        }
    }
}
