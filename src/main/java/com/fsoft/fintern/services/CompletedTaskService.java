package com.fsoft.fintern.services;



import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.CompletedTaskDTO;
import com.fsoft.fintern.enums.TaskStatus;
import com.fsoft.fintern.models.CompletedTasks;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.CompletedTaskRepository;
import com.fsoft.fintern.repositories.TaskRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Service
public class CompletedTaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CompletedTaskRepository completeTaskRepository;


    public CompletedTaskService(ClassroomRepository classRepository, TaskRepository taskRepository, UserRepository userRepository, CompletedTaskRepository completeTaskRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.completeTaskRepository = completeTaskRepository;
    }
    public ResponseEntity<CompletedTasks> createCompletedTask(CompletedTaskDTO completedTaskDTO) throws BadRequestException {
        Task existingTask = this.taskRepository.findById(completedTaskDTO.getTaskId()).orElse(null);
        User existingUser = this.userRepository.findById(completedTaskDTO.getUserId()).orElse(null);



        if (existingUser == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_ALREADY_EXISTS.getMessage());
        }

        if (existingTask == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_IS_EMPTY.getMessage());
        }

        if (existingTask.getDeletedAt() != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_IS_COMPLETED.getMessage());
        }



        CompletedTasks newCompleted = new CompletedTasks();
        newCompleted.setId(new CompletedTaskId(completedTaskDTO.getTaskId(), completedTaskDTO.getUserId()));
        newCompleted.setComment(completedTaskDTO.getComment());
        newCompleted.setTask(existingTask);
        newCompleted.setUser(existingUser);
        newCompleted.setStatus(TaskStatus.COMPLETED);
        newCompleted.setCreatedAt(Timestamp.from(Instant.now()));

        CompletedTasks savedCompTask = this.completeTaskRepository.save(newCompleted);
        return new ResponseEntity<>(savedCompTask, HttpStatus.CREATED);
    }


    public ResponseEntity<List<CompletedTasks>> findAll() throws BadRequestException {
        List<CompletedTasks> completedTasks = this.completeTaskRepository.findAll();

        if (completedTasks.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.COMPLETED_TASK_IS_NOT_FOUND.getMessage());
        } else {
            return new ResponseEntity<>(completedTasks, HttpStatus.OK);
        }
    }


    public ResponseEntity<CompletedTasks> findById(CompletedTaskId completedTaskId) throws BadRequestException {

        Optional<CompletedTasks> completedTask = this.completeTaskRepository.findById(completedTaskId);
        if (completedTask.isPresent()) {
            return new ResponseEntity<>(completedTask.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.COMPLETED_TASK_IS_NOT_FOUND.getMessage());
        }
    }


    public ResponseEntity<CompletedTasks> update(CompletedTaskId completedTaskId, CompletedTaskDTO completedTaskDTO) throws BadRequestException {
        CompletedTasks completedTask = this.completeTaskRepository.findById(completedTaskId).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.TASK_NOT_EXISTS_ID.getMessage()));

        BeanUtils.copyProperties(completedTaskDTO, completedTask, BeanUtilsHelper.getNullPropertyNames(completedTaskDTO));

        this.completeTaskRepository.save(completedTask);
        return new ResponseEntity<>(completedTask, HttpStatus.OK);
    }



    public ResponseEntity<CompletedTasks> delete(CompletedTaskId completedTaskId) throws BadRequestException {

        CompletedTasks completedTask = this.completeTaskRepository.findById(completedTaskId).orElse(null);
        if (completedTask == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_NOT_EXISTS_ID.getMessage());
        }

        this.completeTaskRepository.delete(completedTask);
        return new ResponseEntity<>(completedTask, HttpStatus.OK);
    }

}

