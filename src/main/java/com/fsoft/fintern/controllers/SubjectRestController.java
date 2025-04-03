package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.SubjectDTO;
import com.fsoft.fintern.models.Subject;
import com.fsoft.fintern.services.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/subjects")
public class SubjectRestController {
    private final SubjectService subjectService;

    @Autowired
    public SubjectRestController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("")
    @Operation(description = "Get all subjects with pagination")
    public ResponseEntity<Page<Subject>> getAllSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return this.subjectService.findAllWithPagination(pageable);
    }

    @GetMapping("/all")
    @Operation(description = "Get all subjects without pagination")
    public ResponseEntity<List<Subject>> getAllSubjectsNoPagination() {
        return this.subjectService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(description = "Get subject by id")
    public ResponseEntity<Subject> getSubjectById(@PathVariable int id) throws BadRequestException {
        return this.subjectService.findById(id);
    }

    @PostMapping("/create")
    @Operation(description = "Create a new subject")
    public ResponseEntity<Subject> createSubject(@RequestBody SubjectDTO subjectDTO) throws BadRequestException {
        return this.subjectService.createSubject(subjectDTO);
    }

    @PatchMapping("/update/{id}")
    @Operation(description = "Update subject by id")
    public ResponseEntity<Subject> updateSubject(@RequestBody SubjectDTO subjectDTO, @PathVariable int id) throws BadRequestException {
        return this.subjectService.updateSubject(id, subjectDTO);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(description = "Delete subject by id (soft delete)")
    public ResponseEntity<Subject> deleteSubject(@PathVariable int id) throws BadRequestException {
        return this.subjectService.deleteSubject(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(description = "Restore a deleted subject")
    public ResponseEntity<Subject> restoreSubject(@PathVariable int id) throws BadRequestException {
        return this.subjectService.restoreSubject(id);
    }
}