package com.fsoft.fintern.controllers;


import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.services.CompletedTaskService;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("EmpManagerClass")
public class CompletedTaskController {
   @Autowired
   private CompletedTaskService completedTaskService;

   @Autowired
   private UserService userService;



}
