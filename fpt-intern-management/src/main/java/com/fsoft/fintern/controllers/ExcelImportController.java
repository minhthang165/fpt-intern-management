package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.ExcelImportService;
import org.hibernate.internal.log.SubSystemLogging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {
    private final ExcelImportService excelImportService;

    public ExcelImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importUsers(@RequestPart final MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File cannot be empty!");
        }

        try {
            this.excelImportService.importUsers(file);
            return ResponseEntity.ok("Import data successful!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error data when trying importing: " + e.getMessage());
        }
    }
}

