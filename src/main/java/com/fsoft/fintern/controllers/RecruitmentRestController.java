package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.services.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/api/")
public class RecruitmentRestController {

    private final RecruitmentService recruitmentService;

    public RecruitmentRestController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @GetMapping("/recruitment")
    @Operation(description = "view all Recruitment")
    public ResponseEntity<Page<Recruitment>> viewAllRecruitment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return this.recruitmentService.findAll(pageable);
    }

    @PostMapping("/recruitment/create")
    @Operation(description = "Create a new recruitment")
    public ResponseEntity<Recruitment> create(@RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentService.create(recruitmentDTO);
    }


    @DeleteMapping("/recruitment/delete/{id}")
    @Operation(description = "Delete Task by ID")
    public ResponseEntity<Recruitment> delete(@PathVariable int id) throws BadRequestException {
        return this.recruitmentService.delete(id);
    }


    @PatchMapping("/recruitment/update/{id}")
    @Operation(description = "Update Task by Id")
    public ResponseEntity<Recruitment> update(@PathVariable int id, @RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentService.update(id, recruitmentDTO);
    }

}
