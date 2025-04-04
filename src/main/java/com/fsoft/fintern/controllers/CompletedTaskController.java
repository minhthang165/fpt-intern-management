package com.fsoft.fintern.controllers;


import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.services.CompletedTaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CompletedTaskController {
    private final CompletedTaskService completedTaskService;

    @Autowired
    public CompletedTaskController(CompletedTaskService completedTaskService) {
        this.completedTaskService = completedTaskService;
    }

    @GetMapping("")
    @Operation(description = "view all Completed Task")
    public String viewAllCompletedTask(@SessionAttribute("user") LoginUserDTO loginUserDTO, @RequestParam(name = "user_id", required = false) Integer user_id, Model model) {
        model.addAttribute("user_id", user_id);
        return "employee/EmpManagerClass";
    }
}
