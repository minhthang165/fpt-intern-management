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

    @PostMapping("/import")
    public ResponseEntity<List<SchedulingDTO>> importSchedulingData(@RequestPart final MultipartFile file) throws IOException
    {
        List<SchedulingDTO> data = schedulingService.importSchedulingData(file);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/generate")
    @Operation(description = "Generates class schedule based on provided scheduling data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid scheduling data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ScheduleDTO>> generateSchedule(
            @Parameter(description = "List of scheduling data")
            @RequestBody List<SchedulingDTO> data) {
        List<ScheduleDTO> schedule = schedulingService.generateSchedule(data);
        schedulingService.saveSchedule(schedule);
        return ResponseEntity.ok(schedule);
    }

//    @PostMapping("/save")
//    @Operation(description = "Saves the generated schedule to the database")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Schedule saved successfully"),
//        @ApiResponse(responseCode = "400", description = "Invalid schedule data"),
//        @ApiResponse(responseCode = "500", description = "Internal server error")
//    })
//    public ResponseEntity<?> saveSchedule(
//            @Parameter(description = "List of schedule results to save")
//            @RequestBody List<ScheduleDTO> schedules) {
//        try {
//            boolean success = schedulingService.saveSchedule(schedules);
//            return ResponseEntity.ok(Map.of("success", success, "message", "Lịch học đã được lưu thành công"));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
//        }
//    }
} 