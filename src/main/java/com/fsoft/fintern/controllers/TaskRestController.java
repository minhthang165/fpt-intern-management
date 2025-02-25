package com.fsoft.fintern.controllers;


import com.fsoft.fintern.dtos.TaskDTO;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/task")
public class TaskRestController {
    private final TaskService taskService;

    @Autowired
    public TaskRestController(TaskService taskService) { this.taskService = taskService; }


    @GetMapping("/task")
    @Operation(description = "View all Task")
    public ResponseEntity<List<Task>> findAll() throws BadRequestException {
        return this.taskService.findAll();
    }

    @PostMapping("/task/create")
    @Operation(description = "Create a new Task")
    public ResponseEntity<Task> create(@RequestBody TaskDTO taskDTO) throws BadRequestException {
        return this.taskService.createTask(taskDTO);
    }

    @GetMapping("/task/{id}")
    @Operation(description = "Get Task by ID")
    public ResponseEntity<Task> findById(@PathVariable int id) throws BadRequestException {
        return this.taskService.findById(id);
    }


    @DeleteMapping("/task/delete/{id}")
    @Operation(description = "Delete Task by ID")
    public ResponseEntity<Task> delete(@PathVariable int id) throws BadRequestException {
        return this.taskService.delete(id);
    }

    @PatchMapping("/task/setIsActiveTrue/{id}")
    @Operation(description = "Update IsActive True")
    public ResponseEntity<Task> setIsActiveTrue(@PathVariable int id) throws BadRequestException {
        return this.taskService.setIsActiveTrue(id);
    }

    @PatchMapping("/task/update/{id}")
    @Operation(description = "Update Task by Id")
    public ResponseEntity<Task> update(@PathVariable int id, @RequestBody TaskDTO taskDTO) throws BadRequestException {
        return this.taskService.update(id, taskDTO);
    }
}
