package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.UserDTO;
import com.fsoft.fintern.models.User.User;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final UserService user_service;

    @Autowired
    public UserController(UserService user_service) {
        this.user_service = user_service;
    }

    @GetMapping("/users")
    @Operation(description = "view all user")
    public ResponseEntity<List<User>> findAll() throws BadRequestException {
        return this.user_service.findAll();
    }

    @PostMapping("/users/create")
    @Operation(description = "create new intern")
    public ResponseEntity<User> createIntern(@RequestBody UserDTO user) throws BadRequestException {
        return this.user_service.createIntern(user);
    }

    @GetMapping("/users/{id}")
    @Operation(description =  "Get User by id")
    public ResponseEntity<User> getById(@PathVariable int id) throws BadRequestException {
        return this.user_service.getById(id);
    }

    @PatchMapping("/users/update/{id}")
    @Operation(description = "Update user by id")
    public ResponseEntity<User> update(@RequestBody UserDTO user, @PathVariable int id) throws BadRequestException  {
        return this.user_service.update(id, user);
    }

    @DeleteMapping("/users/delete/{id}")
    @Operation(description = "Delete user by id")
    public ResponseEntity<User> delete(@PathVariable int id) throws BadRequestException {
        return this.user_service.delete(id);
    }

    @GetMapping("/test")
    @Operation(description = "Test thymeleaf dispatcher")
    public ModelAndView login() throws BadRequestException {
        // Fetch user data for user_id = 1
        ResponseEntity<User> userResponse = this.user_service.getById(1);
        User user = userResponse.getBody();

        // Create a ModelAndView object and set the view name to "login.html"
        ModelAndView modelAndView = new ModelAndView("login");

        // Add the user object to the model
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
