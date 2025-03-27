package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("schedule")
public class ScheduleController {

    @GetMapping
    private String viewSchedule(@SessionAttribute("user")LoginUserDTO user, Model model) {
        if (user.getRole() == Role.EMPLOYEE) {
            return "/employee/EmpSchedule";
        } else
            return "/intern/InternSchedule";
    }
}
