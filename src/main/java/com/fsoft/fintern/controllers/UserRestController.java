package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.UpdateUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    public ResponseEntity<String> banUser(@PathVariable int userId, @RequestBody Map<String, Long> request) {
        Long durationInSeconds = request.get("duration");
        String key = "banned_user:" + userId;
        redisTemplate.opsForValue().set(key, "banned", durationInSeconds, TimeUnit.SECONDS);
        return ResponseEntity.ok("User " + userId + " has been banned for " + durationInSeconds + " seconds.");
    }

    //Get banned information
    @GetMapping("/get-status/{userId}")
    public ResponseEntity<Map<String, Object>> checkBanStatus(@PathVariable int userId) {
        String key = "banned_user:" + userId;
        Boolean isBanned = redisTemplate.hasKey(key);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("isBanned", isBanned);

        if (isBanned != null && isBanned) {
            // Get the remaining time
            Long remainingTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            response.put("duration", remainingTime != null ? remainingTime : 0);
        } else {
            response.put("duration", 0);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable int userId) {
        String key = "banned_user:" + userId;
        redisTemplate.opsForHash().delete("banned_users", String.valueOf(userId));
        redisTemplate.delete(key);
        return ResponseEntity.ok("User " + userId + " has been unbanned.");
    }


}
