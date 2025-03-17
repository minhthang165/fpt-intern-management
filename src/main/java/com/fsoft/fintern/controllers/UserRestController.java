package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.UpdateUserDTO;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/user")
public class UserRestController {
    private final UserService userService;
  
    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    @Operation(description = "view all user")
    public ResponseEntity<List<User>> findAll() throws BadRequestException {
        return this.userService.findAll();
    }

    @PostMapping("/create")
    @Operation(description = "create new intern")
    public ResponseEntity<User> createIntern(@RequestBody CreateUserDTO user) throws BadRequestException {
        return this.userService.createIntern(user);
    }

    @GetMapping("/{id}")
    @Operation(description =  "Get User by id")
    public ResponseEntity<User> getById(@PathVariable int id) throws BadRequestException {
        return this.userService.getById(id);
    }

    @PatchMapping("/update/{id}")
    @Operation(description = "Update user by id")
    public ResponseEntity<User> update(@RequestBody UpdateUserDTO user, @PathVariable int id) throws BadRequestException  {
        return this.userService.update(id, user);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(description = "Delete user by id")
    public ResponseEntity<User> delete(@PathVariable int id) throws BadRequestException {
        return this.userService.delete(id);
    }

    @PostMapping("/create-employee")
    @Operation(description = "Create Employee")
    public ResponseEntity<User> createEmployee(@RequestBody CreateUserDTO user) throws BadRequestException {
        return this.userService.createEmployeeOrGuest(user);
    }

    @PatchMapping("/setIsActiveTrue/{id}")
    @Operation(description = "Update isActive true")
    public ResponseEntity<User> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.userService.setIsActiveTrue(id);
    }

    @GetMapping("/email/{email}")
    @Operation(description = "Find user by email")
    public ResponseEntity<User> findByEmail(@PathVariable String email) throws BadRequestException {
        return this.userService.getByEmail(email);
    }
}
