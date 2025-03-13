package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.ExcelExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/user-export")
public class ExcelExportController {
    private final ExcelExportService excelExportService;
    public ExcelExportController(ExcelExportService excelExportService, ExcelExportService excelExportService1) {
        this.excelExportService = excelExportService1;
    }

    @GetMapping("/intern")
    public ResponseEntity<byte[]> exportInternToExcel() throws IOException {
        byte[] excelFile = excelExportService.exportInternUsers();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=INTERN.xlsx");

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/employee")
    public ResponseEntity<byte[]> exportEmployeeToExcel() throws IOException {
        byte[] excelFile = excelExportService.exportEmployeeUsers();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=EMPLOYEE.xlsx");

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<byte[]> exportAdminToExcel() throws IOException {
        byte[] excelFile = excelExportService.exportAdminUsers();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=ADMIN.xlsx");

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/guest")
    public ResponseEntity<byte[]> exportGuestToExcel() throws IOException {
        byte[] excelFile = excelExportService.exportGuestUsers();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=GUEST.xlsx");

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }
}

