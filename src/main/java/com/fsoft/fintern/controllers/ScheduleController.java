package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.services.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("schedule")
public class ScheduleController {

    private final RoomService roomService;

    public ScheduleController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    private String viewSchedule(@SessionAttribute("user")LoginUserDTO user, Model model) {
        model.addAttribute("userId", user.getId());
        model.addAttribute("userRole", user.getRole().toString());
        model.addAttribute("user", user);
        
        // Get rooms and handle empty case
        ResponseEntity<List<Room>> roomsResponse = roomService.findAll();
        List<Room> rooms = roomsResponse.getBody() != null ? roomsResponse.getBody() : new ArrayList<>();
        model.addAttribute("rooms", rooms);
        
        if (user.getRole() == Role.INTERN) {
            model.addAttribute("classId", user.getClass_id());
        }

        if (user.getRole() == Role.EMPLOYEE) {
            return "/employee/EmpSchedule";
        } else if (user.getRole() == Role.INTERN)
            return "/intern/InternSchedule";
        else
            return "/admin/AdminSchedule";
    }
}
