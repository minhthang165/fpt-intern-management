package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CompletedTask;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.repositories.CompletedTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompletedTaskService {

    private final CompletedTaskRepository completedTaskRepository;

    @Autowired
    public CompletedTaskService(CompletedTaskRepository completedTaskRepository) {
        this.completedTaskRepository = completedTaskRepository;
    }

    // Create or update a task
    public CompletedTask saveTask(CompletedTask task) {
        // Validate status
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
}