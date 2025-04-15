package com.fsoft.fintern.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.UpdateUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.apache.el.stream.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/user")
public class UserRestController {
    private final UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("")
    @Operation(description = "view all user")
    public ResponseEntity<Page<User>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return userService.findAll(pageable);
    }

    @PostMapping("/create")
    @Operation(description = "create new intern")
    public ResponseEntity<User> createIntern(@RequestBody CreateUserDTO user) throws BadRequestException {
        return this.userService.createIntern(user);
    }

    @GetMapping("/{id}")
    @Operation(description = "Get User by id")
    public ResponseEntity<User> getById(@PathVariable int id) throws BadRequestException {
        return this.userService.getById(id);
    }

    @PatchMapping("/update/{id}")
    @Operation(description = "Update user by id")
    public ResponseEntity<User> update(@RequestBody UpdateUserDTO user, @PathVariable int id) throws BadRequestException {
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

//    @GetMapping("/{email}")
//    @Operation(description = "Find user by email")
//    public ResponseEntity<User> findByEmail(@PathVariable String email) throws BadRequestException {
//        return this.userService.getByEmail(email);
//    }

    @GetMapping("/role/{role}")
    @Operation(description = "find user by role")
    public ResponseEntity<Page<User>> findByRole(
            @PathVariable Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return this.userService.findUserByRole(role, pageable);
    }

    // Ban a user for a specified duration
    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable int userId, @RequestBody Map<String, Object> request) {
        try {
            Long durationInSeconds = Long.valueOf(request.get("duration").toString());
            durationInSeconds *= 3600*24;
            String reason = (String) request.get("reason");
            return ResponseEntity.ok(userService.banUser(userId, durationInSeconds, reason));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing ban info.");
        }
    }

    //Get banned information
    @GetMapping("/get-status/{userId}")
    public ResponseEntity<Map<String, Object>> getBanStatus(@PathVariable int userId) {
        try {
            return ResponseEntity.ok(userService.getBanStatus(userId));
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(Map.of("userId", userId, "banned", false));
        }
    }

    @DeleteMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable int userId) {
        return ResponseEntity.ok(userService.unbanUser(userId));
    }

    //Get all the users that their email contain input string
    @GetMapping("/search/users")
    public ResponseEntity<List<User>> getAllUsersByEmail(@RequestParam String email) {
        return userService.searchUsersByEmail(email);
    }

    @GetMapping("/classroom/{classId}")
    public ResponseEntity<List<User>> findUserByClassroom_Id(@PathVariable int classId) {
        return userService.findUserByClassroom_Id(classId);
    }
}
