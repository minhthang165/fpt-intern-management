package com.fsoft.fintern.controllers;

import com.fsoft.fintern.models.CompletedTask;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.services.CompletedTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/completed-tasks")
public class CompletedTaskRestController {

    private final CompletedTaskService completedTaskService;

    @Autowired
    public CompletedTaskRestController(CompletedTaskService completedTaskService) {
        this.completedTaskService = completedTaskService;
    }

    // Create or update a task
    @PostMapping("create")
    public ResponseEntity<CompletedTask> createTask(@RequestBody CompletedTask task) {
        CompletedTask savedTask = completedTaskService.saveTask(task);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    // Get all tasks
    @GetMapping
    public ResponseEntity<List<CompletedTask>> getAllTasks() {
        List<CompletedTask> tasks = completedTaskService.getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // Get a task by composite ID (task_id, user_id, class_id)
    @GetMapping("/{taskId}/{userId}/{classId}")
    public ResponseEntity<CompletedTask> getTaskById(
            @PathVariable Integer taskId,
            @PathVariable Integer userId,
            @PathVariable Integer classId) {
        CompletedTaskId id = new CompletedTaskId(taskId, userId, classId);
        return completedTaskService.getTaskById(id)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete a task (soft delete)
    @DeleteMapping("/{taskId}/{userId}/{classId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Integer taskId,
            @PathVariable Integer userId,
            @PathVariable Integer classId) {
        CompletedTaskId id = new CompletedTaskId(taskId, userId, classId);
        completedTaskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("find/{taskId}/{userId}/{classId}")
    public ResponseEntity<CompletedTask> getCompletedTask(
            @PathVariable int taskId,
            @PathVariable int userId,
            @PathVariable int classId) {

        return completedTaskService
                .getCompletedTaskByTaskIdAndUserIdAndClassId(taskId, userId, classId)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    // Exception handler for invalid status
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/by-task-and-class/{taskId}/{classId}")
    public ResponseEntity<List<CompletedTask>> listCompletedTask(
            @PathVariable int taskId,
            @PathVariable int classId) {
        List<CompletedTask> tasks = completedTaskService.listCompletedTask(taskId, classId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
    @PatchMapping("/{taskId}/{userId}/{classId}/mark")
    public ResponseEntity<?> updateCompletedTaskMark(
            @PathVariable int taskId,
            @PathVariable int userId,
            @PathVariable int classId,
            @RequestBody Integer mark) {
        try {
            CompletedTaskId id = new CompletedTaskId(taskId, userId, classId);
            CompletedTask updated = completedTaskService.updateMark(id, mark);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the mark", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PatchMapping("/{taskId}/{userId}/{classId}/comment")
    public ResponseEntity<?> updateCompletedTaskComment(
            @PathVariable int taskId,
            @PathVariable int userId,
            @PathVariable int classId,
            @RequestBody String comment) {
        try {
            CompletedTaskId id = new CompletedTaskId(taskId, userId, classId);
            CompletedTask updated = completedTaskService.updateComment(id, comment);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the comment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Generic exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}