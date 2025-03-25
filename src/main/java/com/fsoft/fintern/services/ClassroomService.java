package com.fsoft.fintern.services;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.ClassroomDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomService {
    private final ClassroomRepository class_repository;
    private final UserRepository userRepository;

    public ClassroomService(ClassroomRepository class_repository, UserRepository userRepository) {
        this.class_repository = class_repository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Classroom> createClass(ClassroomDTO classDTO) throws BadRequestException {
        Classroom existed_class = findClassroomByName(classDTO.getClassName());
        if (existed_class != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_ALREADY_EXISTS.getMessage());
        }

        User manager = this.userRepository.findById(classDTO.getManagerId()).orElse(null);

        if (manager == null || manager.getRole() != Role.EMPLOYEE) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }

        Classroom newClass = new Classroom();
        newClass.setClassName(classDTO.getClassName());
        newClass.setNumberOfIntern(classDTO.getNumberOfIntern());
        newClass.setStatus(classDTO.getStatus());
        newClass.setManager(manager);

        Classroom savedClass = class_repository.save(newClass);
        return new ResponseEntity<>(savedClass, HttpStatus.CREATED);
    }

    public ResponseEntity<Page<Classroom>> findAll(Pageable pageable) throws BadRequestException {
        Page<Classroom> classrooms = class_repository.findAll(pageable);
        if(classrooms.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_IS_EMPTY.getMessage());
        } else {
            return new ResponseEntity<>(classrooms, HttpStatus.OK);
        }
    }

    public ResponseEntity<Classroom> findById(int id) throws BadRequestException {
        Optional<Classroom> classroom = this.class_repository.findById(id);
        if(classroom.isPresent()) {
            return new ResponseEntity<>(classroom.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }
    }

    public ResponseEntity<Classroom> findClassByName(String name) throws BadRequestException {
        Optional<Classroom> classroom = this.class_repository.findClassroomByClassName(name);
        if(classroom.isPresent()) {
            return new ResponseEntity<>(classroom.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_NAME.getMessage());
        }
    }

    public ResponseEntity<Classroom> update(int id, ClassroomDTO classDTO) throws BadRequestException {
        Classroom classroom = this.class_repository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage())
        );

        if (classDTO.getManagerId() != null) {
            User manager = this.userRepository.findById(classDTO.getManagerId()).orElseThrow(()
                    -> new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage())
            );
            BeanUtils.copyProperties(classDTO, classroom, BeanUtilsHelper.getNullPropertyNames(classDTO));
            classroom.setManager(manager);

            this.class_repository.save(classroom);
            return new ResponseEntity<>(classroom, HttpStatus.OK);
        }

        BeanUtils.copyProperties(classDTO, classroom, BeanUtilsHelper.getNullPropertyNames(classDTO));
        this.class_repository.save(classroom);
        return new ResponseEntity<>(classroom, HttpStatus.OK);
    }

    public ResponseEntity<Classroom> delete(int id) throws BadRequestException {
        Classroom classroom = this.class_repository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage())
        );
        classroom.setActive(false);
        classroom.setDeletedAt(Timestamp.from(Instant.now().plus(Duration.ofHours(7))));

        this.class_repository.save(classroom);
        return new ResponseEntity<>(classroom, HttpStatus.OK);
    }

    private Classroom findClassroomByName(String name) {
        Optional<Classroom> existed_class =  this.class_repository.findClassroomByClassName(name);
        if (existed_class.isPresent()) {
            return existed_class.get();
        }
        return null;
    }

    public ResponseEntity<Classroom> setIsActiveTrue(int id) throws BadRequestException {
        Classroom existedClassroom = this.class_repository.findById(id).orElse(null);
        if (existedClassroom == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        if (existedClassroom.isActive()) {
            throw new BadRequestException(ErrorDictionaryConstraints.IS_ACTIVE_TRUE.getMessage());
        }

        existedClassroom.setActive(true);
        this.class_repository.save(existedClassroom);
        return new ResponseEntity<>(existedClassroom, HttpStatus.OK);
    }

}
