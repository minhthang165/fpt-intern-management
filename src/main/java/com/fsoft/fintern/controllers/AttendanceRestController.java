package com.fsoft.fintern.controllers;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.AttendanceDTO;
import com.fsoft.fintern.models.Attendance;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.repositories.AttendanceRepository;
import com.fsoft.fintern.services.AttendanceService;
import com.fsoft.fintern.services.ScheduleService;
import com.fsoft.fintern.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/attendance")
public class AttendanceRestController {
    private final AttendanceService attendanceService;


    @Autowired
    public AttendanceRestController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/")
    @Operation(description = "View all Attendance")
    public ResponseEntity<List<Attendance>> findAll(AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.findAll(attendanceDTO);
    }

    @PostMapping("/create")
    @Operation(description = "Create a new Attendance")
    public ResponseEntity<Attendance> create(@Valid AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.createAttendance(attendanceDTO);
    }

    @GetMapping("/{id}")
    @Operation(description = "Get Attendance by ID")
    public ResponseEntity<Attendance> findById(@PathVariable int id) throws BadRequestException {
        return this.attendanceService.findById(id);
    }


    @PatchMapping("/updatePresent/{id}")
    @Operation(description = "Update Attendance to Present by Id")
    public ResponseEntity<Attendance> updateToPresent(@PathVariable int id, @RequestBody AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.updateAttendancetoPresent(id, attendanceDTO);
    }


    @PatchMapping("/updateAbsent/{id}")
    @Operation(description = "Update Attendance to Absent by Id")
    public ResponseEntity<Attendance> updateToAbsent(@PathVariable int id, @RequestBody AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.updateAttendanceToAbsent(id, attendanceDTO);
    }

    @GetMapping("/class/{id}")
    @Operation(summary = "Find Intern Attendance by classId")
    public ResponseEntity<List<Attendance>> getAttendanceByClassId(@PathVariable("id") Integer classId) throws BadRequestException {
        return this.attendanceService.findAttendanceByClassId(classId);
    }

}