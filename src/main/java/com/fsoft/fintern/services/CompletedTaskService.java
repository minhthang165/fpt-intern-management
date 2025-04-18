package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.UserTaskDTO;
import com.fsoft.fintern.models.CompletedTask;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.CompletedTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fsoft.fintern.models.User;

import java.util.*;

@Service
public class CompletedTaskService {

    private final CompletedTaskRepository completedTaskRepository;
    private final ClassroomRepository class_repository;
    @Autowired
    public CompletedTaskService(CompletedTaskRepository completedTaskRepository, ClassroomRepository classRepository) {
        this.completedTaskRepository = completedTaskRepository;
        class_repository = classRepository;
    }

    // Create or update a task
    public CompletedTask saveTask(CompletedTask task) {
        if (!isValidStatus(task.getStatus())) {
            throw new IllegalArgumentException("Status must be PENDING, COMPLETED, or REJECTED");
        }
        return completedTaskRepository.save(task);
    }

    // Get all tasks
    public List<CompletedTask> getAllTasks() {
        return completedTaskRepository.findAll();
    }

    // Get a task by ID
    public Optional<CompletedTask> getTaskById(CompletedTaskId id) {
        return completedTaskRepository.findById(id);
    }

    // Delete a task (soft delete by setting deleted_at and is_active)
    public void deleteTask(CompletedTaskId id) {
        Optional<CompletedTask> taskOpt = completedTaskRepository.findById(id);
        if (taskOpt.isPresent()) {
            CompletedTask task = taskOpt.get();
            task.setIsActive(false);
            completedTaskRepository.save(task);
        } else {
            throw new RuntimeException("Task not found with ID: " + id);
        }
    }
    public Optional<CompletedTask> getCompletedTaskByTaskIdAndUserIdAndClassId(int taskId, int userId, int classId) {
        return completedTaskRepository.findByTaskIdAndUserIdAndClassroomId(taskId, userId, classId);
    }
    public CompletedTask updateComment(CompletedTaskId id, String comment) {
        Optional<CompletedTask> existingTaskOpt = completedTaskRepository.findById(id);

        if (!existingTaskOpt.isPresent()) {
            throw new RuntimeException("Task not found with ID: " + id);
        }

        CompletedTask existingTask = existingTaskOpt.get();

        // Cập nhật comment nếu được cung cấp
        if (comment != null) {
            existingTask.setComment(comment);
        }

        return completedTaskRepository.save(existingTask);
    }

    public CompletedTask updateMark(CompletedTaskId id, Integer mark) {
        Optional<CompletedTask> existingTaskOpt = completedTaskRepository.findById(id);

        if (!existingTaskOpt.isPresent()) {
            throw new RuntimeException("Task not found with ID: " + id);
        }

        CompletedTask existingTask = existingTaskOpt.get();

        // Cập nhật mark và trạng thái nếu mark được cung cấp
        if (mark != null) {
            existingTask.setMark(mark);
            existingTask.setStatus("COMPLETED");
        }

        return completedTaskRepository.save(existingTask);
    }
    public List<CompletedTask> listCompletedTask(int taskId, int classId) {
        return completedTaskRepository.findByTaskIdAndClassroomId(taskId, classId);
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) || "COMPLETED".equals(status) || "REJECTED".equals(status);
    }
    public List<UserTaskDTO> getUsersWithCompletedTasks(Integer classId, Integer taskId) {
        // Get list of users by classId
        List<User> users = class_repository.findByClassIdAndIsActiveTrue(classId);

        // Create list to store UserTaskDTOs
        List<UserTaskDTO> userTaskList = new ArrayList<>();

        // Iterate through users and get their completed task (if any)
        for (User user : users) {
            Optional<CompletedTask> completedTask = completedTaskRepository
                    .findByTaskIdAndUserIdAndClassroomId(taskId, user.getId(), classId);

            // Add UserTaskDTO with user and their completed task (or null if not found)
            userTaskList.add(new UserTaskDTO(user, completedTask.orElse(null)));
        }

        return userTaskList;
    }


}