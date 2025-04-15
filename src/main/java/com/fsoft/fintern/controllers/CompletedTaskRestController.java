package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.CompletedTaskDTO;
import com.fsoft.fintern.models.CompletedTasks;

import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.services.CompletedTaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("api/completedTask")
public class CompletedTaskRestController {
    private final CompletedTaskService completedTaskService;

    @Autowired
    public CompletedTaskRestController(CompletedTaskService completedTaskService) {
        this.completedTaskService = completedTaskService;
    }

    @GetMapping("/completedTask")
    @Operation(description = "View all Completed Task")
    public ResponseEntity<List<CompletedTasks>> getCompletedTasks() throws BadRequestException {
        return this.completedTaskService.findAll();
    }

    @PostMapping("/create")
    @Operation(description = "Create a Completed Task")
    public ResponseEntity<CompletedTasks> createCompletedTask(@RequestBody CompletedTaskDTO completedTaskDTO) throws BadRequestException {
        return this.completedTaskService.createCompletedTask(completedTaskDTO);
    }

    @GetMapping("/{taskId}/{userId}")
    @Operation(description = "Get Completed Task by ID")
    public ResponseEntity<CompletedTasks> findById(@PathVariable int taskId , @PathVariable int userId) throws BadRequestException {
        CompletedTaskId completedTaskId = new CompletedTaskId(taskId, userId);
        return this.completedTaskService.findById(completedTaskId);
    }

    @DeleteMapping("/delete/{taskId}/{userId}")
    @Operation(description = "Delete Complete Task by ID")
    public ResponseEntity<CompletedTasks> deleteCompletedTask(@PathVariable int taskId, @PathVariable int userId) throws BadRequestException {
        CompletedTaskId completedTaskId = new CompletedTaskId(taskId, userId);
        return this.completedTaskService.delete(completedTaskId);

    }

    @PatchMapping("/update/{taskId}/{userId}")
    @Operation(description = "Update Completed Task by Id")
    public ResponseEntity<CompletedTasks> updateCompletedTask(@PathVariable int userId, @PathVariable int taskId, @RequestBody CompletedTaskDTO completedTaskDTO) throws BadRequestException {
        CompletedTaskId completedTaskId = new CompletedTaskId(taskId, userId);
        return this.completedTaskService.update(completedTaskId, completedTaskDTO);
    }
}