package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.ScheduleDTO;
import com.fsoft.fintern.dtos.SchedulingDTO;
import com.fsoft.fintern.models.Schedule;
import com.fsoft.fintern.services.ScheduleService;
import com.fsoft.fintern.services.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingRestController {

    private final SchedulingService schedulingService;
    private final ScheduleService scheduleService;

    @Autowired
    public SchedulingRestController(SchedulingService schedulingService, ScheduleService scheduleService) {
        this.schedulingService = schedulingService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/all")
    @Operation(description = "Get all subjects without pagination")
    public ResponseEntity<List<Schedule>> getAllScheduleNoPagination() {
        return this.scheduleService.findAll();
    }

    @GetMapping("/user/{userId}")
    @Operation(description = "Get schedules for a specific user")
    public ResponseEntity<List<Schedule>> getSchedulesByUserId(@PathVariable Integer userId) {
        try {
            List<Schedule> userSchedules = this.scheduleService.findSchedulesByUserId(userId);
            if (userSchedules.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(userSchedules, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/class/{classId}")
    @Operation(description = "Get schedules for a specific classroom")
    public ResponseEntity<List<Schedule>> getSchedulesByClassId(@PathVariable Integer classId) {
        try {
            List<Schedule> classSchedules = this.scheduleService.findSchedulesByClassId(classId);
            if (classSchedules.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(classSchedules, HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/date/{date}")
    @Operation(description = "Get schedules for a specific date (format: YYYY-MM-DD)")
    public ResponseEntity<List<Schedule>> getSchedulesByDate(@PathVariable String date) {
        try {
            // Parse string date to LocalDate
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
          
            // Find schedules for the date
            List<Schedule> dateSchedules = this.scheduleService.findSchedulesByDate(parsedDate);

            // Find schedules for the date
            List<Schedule> dateSchedules = this.scheduleService.findSchedulesByDate(parsedDate);

            if (dateSchedules.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(dateSchedules, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/template")
    @Operation(description = "Generates and returns an Excel template for class scheduling data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> generateTemplate() throws IOException {
        byte[] excelContent = schedulingService.generateTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "scheduling_template.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }

    @PostMapping("/generate")
    @Operation(description = "Generates class schedule based on provided scheduling data")
    public ResponseEntity<List<ScheduleDTO>> generateSchedule(
            @Parameter(description = "List of scheduling data")
            @RequestPart final MultipartFile file) throws IOException {
        List<SchedulingDTO> data = schedulingService.importSchedulingData(file);
        List<ScheduleDTO> schedule = schedulingService.generateSchedule(data);
        schedulingService.saveSchedule(schedule);
        return ResponseEntity.ok(schedule);
    }
}