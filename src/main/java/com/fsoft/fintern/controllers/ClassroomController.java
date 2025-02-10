package com.fsoft.fintern.controllers;
import com.fsoft.fintern.dtos.ClassroomDTO;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.services.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/class")
public class ClassroomController {
    private final ClassroomService classroomService;

    @Autowired
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("/class")
    @Operation(description = "view all classroom")
    public ResponseEntity<List<Classroom>> findAll() throws BadRequestException {
        return this.classroomService.findAll();
    }

    @PostMapping("/class/create")
    @Operation(description = "create new class")
    public ResponseEntity<Classroom> create(@RequestBody ClassroomDTO classroomDTO) throws BadRequestException {
        return this.classroomService.createClass(classroomDTO);
    }

    @GetMapping("/class/{id}")
    @Operation(description =  "Get Classroom by id")
    public ResponseEntity<Classroom> getById(@PathVariable int id) throws BadRequestException {
        return this.classroomService.findById(id);
    }

    @PatchMapping("/class/update/{id}")
    @Operation(description = "Update class by id")
    public ResponseEntity<Classroom> update(@RequestBody ClassroomDTO classroomDTO, @PathVariable int id) throws BadRequestException  {
        return this.classroomService.update(id, classroomDTO);
    }

    @DeleteMapping("/class/delete/{id}")
    @Operation(description = "Delete class by id")
    public ResponseEntity<Classroom> delete(@PathVariable int id) throws BadRequestException {
        return this.classroomService.delete(id);
    }

    @PatchMapping("/class/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Classroom> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.classroomService.setIsActiveTrue(id);
    }
}
