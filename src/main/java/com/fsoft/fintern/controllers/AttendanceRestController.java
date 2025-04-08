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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("api/attendance")
public class AttendanceRestController {
   private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceRestController(AttendanceService attendanceService, AttendanceRepository attendanceRepository) {
       this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
    }

   @GetMapping("/")
   @Operation(description = "View all Attendance")
   public ResponseEntity<List<AttendanceDTO>> findAll() throws BadRequestException {
       return this.attendanceService.findAll();
   }

   @PostMapping("/create")
    @Operation(description = "Create a new Attendance")
    public ResponseEntity<Attendance> create(@Valid AttendanceDTO attendanceDTO) throws BadRequestException {
       return this.attendanceService.createAttendance(attendanceDTO);
   }

    @GetMapping("/{id}")
    @Operation(description = "Get Attendance by ID")
    public ResponseEntity<AttendanceDTO> findById(@PathVariable int id) throws BadRequestException {
        return this.attendanceService.findById(id);
    }


    @PatchMapping("/updatePresent/{id}")
    @Operation(description = "Update Attendance to Present by Id")
    public ResponseEntity<AttendanceDTO> updateToPresent(@PathVariable int id, @RequestBody AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.updateAttendancetoPresent(id, attendanceDTO);
    }


    @PatchMapping("/updateAbsent/{id}")
    @Operation(description = "Update Attendance to Absent by Id")
    public ResponseEntity<AttendanceDTO> updateToAbsent(@PathVariable int id, @RequestBody AttendanceDTO attendanceDTO) throws BadRequestException {
        return this.attendanceService.updateAttendanceToAbsent(id, attendanceDTO);
    }

    @GetMapping("/class/{id}")
    @Operation(summary = "Find Intern Attendance by classId")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByClassId(@PathVariable("id") Integer classId) throws BadRequestException {
        List<AttendanceDTO> result = attendanceService.getAttendanceByClassId(classId);
        if (result.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }
        return ResponseEntity.ok(result);
    }

}