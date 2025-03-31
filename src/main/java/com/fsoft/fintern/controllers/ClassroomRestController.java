package com.fsoft.fintern.controllers;
import com.fsoft.fintern.dtos.ClassroomDTO;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.services.ClassroomService;
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
@RequestMapping("api/class")
public class ClassroomRestController {
    private final ClassroomService classroomService;

    @Autowired
    public ClassroomRestController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("")
    @Operation(description = "view all classroom")
    public ResponseEntity<Page<Classroom>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return this.classroomService.findAll(pageable);
    }

    @PostMapping("/create")
    @Operation(description = "create new class")
    public ResponseEntity<Classroom> create(@RequestBody ClassroomDTO classroomDTO) throws BadRequestException {
        return this.classroomService.createClass(classroomDTO);
    }

    @GetMapping("/{id}")
    @Operation(description =  "Get Classroom by id")
    public ResponseEntity<Classroom> getById(@PathVariable int id) throws BadRequestException {
        return this.classroomService.findById(id);
    }

    @PatchMapping("/update/{id}")
    @Operation(description = "Update class by id")
    public ResponseEntity<Classroom> update(@RequestBody ClassroomDTO classroomDTO, @PathVariable int id) throws BadRequestException  {

        return this.classroomService.update(id, classroomDTO);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(description = "Delete class by id")
    public ResponseEntity<Classroom> delete(@PathVariable int id) throws BadRequestException {
        return this.classroomService.delete(id);
    }

    @GetMapping("/{classId}/users")
    public ResponseEntity<List<User>> getUsersByClass(@PathVariable Integer classId) {
        List<User> users = classroomService.getUsersByClassId(classId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{mentorId}/")
    public ResponseEntity<List<Classroom>> getClassByMentor(@PathVariable Integer mentorId) {
        List<Classroom> classrooms = classroomService.getClassroomsByMentorId(mentorId);
        return ResponseEntity.ok(classrooms);
    }

    @PatchMapping("/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Classroom> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.classroomService.setIsActiveTrue(id);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Classroom>> getAllClassroomsNoPagination() throws BadRequestException {
        return classroomService.findAlls();
    }

}
