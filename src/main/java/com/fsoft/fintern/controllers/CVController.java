package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CVSubmitterDTO;
import com.fsoft.fintern.models.CVSubmitter;
import com.fsoft.fintern.services.CVSubmitterService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/CVSubmitter")
public class CVController {
    private final CVSubmitterService cvSubmitterService;

    public CVController(CVSubmitterService cvSubmitterService) {
        this.cvSubmitterService = cvSubmitterService;
    }

    @PostMapping("/create")
    @Operation(description = "Submit a CV")
    public ResponseEntity<CVSubmitter> create(@RequestBody CVSubmitterDTO dto) {
        CVSubmitter cvSubmitter = cvSubmitterService.create(dto.getRecruitmentId(), dto.getFileId());
        return ResponseEntity.ok(cvSubmitter);
    }
}
