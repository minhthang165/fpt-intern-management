package com.fsoft.fintern.controllers;


import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.services.CVInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cv-info")
public class CVInfoRestController {

    private final CVInfoService cvInfoService;

    @Autowired
    public CVInfoRestController(CVInfoService cvInfoService) {
        this.cvInfoService = cvInfoService;
    }

    @GetMapping
    public ResponseEntity<List<CVInfo>> getAllActiveCVs() {
        List<CVInfo> cvInfoList = cvInfoService.getAllActiveCVs();
        return new ResponseEntity<>(cvInfoList, HttpStatus.OK);
    }

    @PostMapping("/filter-by-gpa")
    public String filterCVs() {
        cvInfoService.filterCVsByMinGPA();
        return "CVs below minimum GPA have been removed";
    }

}
