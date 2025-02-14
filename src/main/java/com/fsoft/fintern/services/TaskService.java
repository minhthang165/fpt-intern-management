package com.fsoft.fintern.services;


import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.ClassroomDTO;
import com.fsoft.fintern.dtos.TaskDTO;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
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
public class TaskService {
    private final TaskRepository taskRepository;
    private final ClassroomRepository classRepository;
    private final UserRepository userRepository;


    public TaskService(TaskRepository taskRepository, ClassroomRepository classRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;

    }

    public ResponseEntity<Task> findById(int id) throws BadRequestException {
        Optional<Task> task = this.taskRepository.findById(id);
        if (task.isPresent()) {
            return new ResponseEntity<>(task.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_NOT_EXISTS_ID.getMessage());
        }
    }

    public ResponseEntity<List<Task>> findAll() {
        List<Task> tasks = this.taskRepository.findAll();
        if (tasks.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        }
    }

    public ResponseEntity<Task> update(int id, TaskDTO taskDTO) throws BadRequestException {
        Task task = this.taskRepository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.TASK_NOT_EXISTS_ID.getMessage()));

        BeanUtils.copyProperties(taskDTO, task, BeanUtilsHelper.getNullPropertyNames(taskDTO));

        this.taskRepository.save(task);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    public ResponseEntity<Task> delete(int id) throws BadRequestException {
        Task task = this.taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_NOT_EXISTS_ID.getMessage());
        }
        task.setActive(false);
        task.setDeletedAt(Timestamp.from(Instant.now().plus((Duration.ofHours(6)))));
        this.taskRepository.save(task);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    public ResponseEntity<Task> createTask(TaskDTO taskDTO) throws BadRequestException {
        Task existingTask = findTaskByName(taskDTO.getTaskName());
        Classroom existedClass = this.classRepository.findById(taskDTO.getClassId()).orElse(null);
        User creator = this.userRepository.findById(taskDTO.getCreator()).orElse(null);

        if (existingTask != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.TASK_IS_ALREADY_EXISTS.getMessage());
        }

        if (existedClass == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        if (taskDTO.getStartTime().after(taskDTO.getEndTime())) {
            throw new BadRequestException(ErrorDictionaryConstraints.INVALID_TIME.getMessage());
        }

        if(creator == null){
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }

        Task newTask = new Task();
        newTask.setTaskName(taskDTO.getTaskName());
        newTask.setFileData(taskDTO.getFileData());
        newTask.setStartTime(taskDTO.getStartTime());
        newTask.setEndTime(taskDTO.getEndTime());
        newTask.setClassroom(existedClass);
        newTask.setCreatedBy(taskDTO.getCreator());


        Task savedTask = this.taskRepository.save(newTask);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    public ResponseEntity<Task> setIsActiveTrue(int id) throws BadRequestException {
        Task existedTask = this.taskRepository.findById(id).orElse(null);
        if (existedTask == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        if (existedTask.isActive()) {
            throw new BadRequestException(ErrorDictionaryConstraints.IS_ACTIVE_TRUE.getMessage());
        }

        existedTask.setActive(true);
        taskRepository.save(existedTask);
        return new ResponseEntity<>(existedTask, HttpStatus.OK);
    }

    private Task findTaskByName(String taskName) {
        Optional<Task> task = this.taskRepository.findTaskByTaskName(taskName);

        if (task.isPresent()) {
            return task.get();
        } else {
            return null;
        }
    }


}




