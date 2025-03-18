package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.ExcelImportService;
import org.hibernate.internal.log.SubSystemLogging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {
    private final ExcelImportService excelImportService;

    public ExcelImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importUsers(@RequestPart final MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("result", null);
            response.put("message", "File cannot be empty!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            int recordsInserted = this.excelImportService.importUsers(file);

            if (recordsInserted == 0) {
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("result", "No new data added");
                response.put("message", "No new records were imported because they already exist.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response.put("status", HttpStatus.OK.value());
            response.put("result", "Inserted " + recordsInserted + " records");
            response.put("message", "Import data successful!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("result", null);
            response.put("message", "Error occurred while importing: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

