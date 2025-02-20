package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.dtos.TaskDTO;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.services.RecruitmentServices;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/recruitment")
public class RecruitmentController {
    private final RecruitmentServices recruitmentServices;

    public RecruitmentController(RecruitmentServices recruitmentServices) {
        this.recruitmentServices = recruitmentServices;
    }

    @GetMapping("/recruitment")
    @Operation(description = "View all recruitment")
    public ResponseEntity<List<Recruitment>> findAll() throws BadRequestException {
        return this.recruitmentServices.findAll();
    }
    @PostMapping("/recruitment/create")
    @Operation(description = "Create a new recruitment")
    public ResponseEntity<Recruitment> create(@RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentServices.create(recruitmentDTO);
    }

    @GetMapping("/recruitment/{id}")
    @Operation(description = "Get recruitment by ID")
    public ResponseEntity<Recruitment> findById(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.findById(id);
    }


    @DeleteMapping("/recruitment/delete/{id}")
    @Operation(description = "Delete Task by ID")
    public ResponseEntity<Recruitment> delete(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.delete(id);
    }


    @PatchMapping("/recruitment/update/{id}")
    @Operation(description = "Update Task by Id")
    public ResponseEntity<Recruitment> update(@PathVariable int id, @RequestBody RecruitmentDTO recruitmentDTO) throws BadRequestException {
        return this.recruitmentServices.update(id, recruitmentDTO);
    }

    @PatchMapping("/recruitment/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Recruitment> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.setIsActiveTrue(id);
    }



    @PatchMapping("/recruitment/setIsActiveFalse/{id}")
    @Operation(description = "Update  IsActive False")
    public ResponseEntity<Recruitment> setIsActiveFalse(@PathVariable int id) throws BadRequestException {
        return this.recruitmentServices.setIsActiveFalse(id);
    }


}