package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.SubjectDTO;
import com.fsoft.fintern.models.Subject;
import com.fsoft.fintern.repositories.SubjectRepository;
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
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public ResponseEntity<List<Subject>> findAll() {
        List<Subject> subjects = this.subjectRepository.findAll();
        if (subjects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(subjects, HttpStatus.OK);
        }
    }

    public ResponseEntity<Page<Subject>> findAllWithPagination(Pageable pageable) throws BadRequestException {
        Page<Subject> subjects = this.subjectRepository.findAll(pageable);
        if (subjects.isEmpty()) {
            throw new BadRequestException("No subjects found");
        } else {
            return new ResponseEntity<>(subjects, HttpStatus.OK);
        }
    }

    public ResponseEntity<Subject> findById(int id) throws BadRequestException {
        Optional<Subject> subject = this.subjectRepository.findById(id);
        if (subject.isPresent()) {
            return new ResponseEntity<>(subject.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException("Subject not found with id: " + id);
        }
    }

    public ResponseEntity<Subject> createSubject(SubjectDTO subjectDTO) throws BadRequestException {
        Optional<Subject> existingSubject = this.subjectRepository.findBySubjectName(subjectDTO.getSubjectName());
        if (existingSubject.isPresent()) {
            throw new BadRequestException("Subject with name '" + subjectDTO.getSubjectName() + "' already exists");
        }

        Subject newSubject = new Subject();
        newSubject.setSubjectName(subjectDTO.getSubjectName());
        newSubject.setDescription(subjectDTO.getDescription());

        Subject savedSubject = this.subjectRepository.save(newSubject);
        return new ResponseEntity<>(savedSubject, HttpStatus.CREATED);
    }

    public ResponseEntity<Subject> updateSubject(int id, SubjectDTO subjectDTO) throws BadRequestException {
        Subject subject = this.subjectRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Subject not found with id: " + id)
        );

        // Check if subject name is being changed and if it conflicts
        if (!subjectDTO.getSubjectName().equals(subject.getSubjectName())) {
            Optional<Subject> subjectWithSameName = this.subjectRepository.findBySubjectName(subjectDTO.getSubjectName());
            if (subjectWithSameName.isPresent()) {
                throw new BadRequestException("Subject with name '" + subjectDTO.getSubjectName() + "' already exists");
            }
        }

        BeanUtils.copyProperties(subjectDTO, subject, BeanUtilsHelper.getNullPropertyNames(subjectDTO));
        this.subjectRepository.save(subject);
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }

    public ResponseEntity<Subject> deleteSubject(int id) throws BadRequestException {
        Subject subject = this.subjectRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Subject not found with id: " + id)
        );
        subject.setActive(false);
        subject.setDeletedAt(Timestamp.from(Instant.now().plus(Duration.ofHours(7))));
        
        this.subjectRepository.save(subject);
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }

    public ResponseEntity<Subject> restoreSubject(int id) throws BadRequestException {
        Subject subject = this.subjectRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Subject not found with id: " + id)
        );
        
        if (subject.isActive()) {
            throw new BadRequestException("Subject is already active");
        }
        
        subject.setActive(true);
        subject.setDeletedAt(null);
        
        this.subjectRepository.save(subject);
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }
}