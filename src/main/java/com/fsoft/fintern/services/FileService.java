package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.FileDTO;
import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.models.File;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.FileRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    public FileService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }
    public ResponseEntity<File> createFile(FileDTO fileDTO) throws BadRequestException {
        if (fileDTO.getSubmitterId() == null) {
            throw new BadRequestException("Submitter ID cannot be null");
        }
        User submitter = userRepository.findById(fileDTO.getSubmitterId())
                .orElseThrow(() -> new BadRequestException("Submitter not found"));

        File newFile = new File();
        newFile.setSubmitter(submitter);
        newFile.setDisplayName(fileDTO.getDisplayName());
        newFile.setPath(fileDTO.getPath());


        File savedFile = fileRepository.save(newFile);
        return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
    }
    public ResponseEntity<String> getPathByFileId(int fileId) {
        Optional<File> file = fileRepository.findById(fileId);
        return file.map(value -> new ResponseEntity<>(value.getPath(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<File> findCVByUserId(Integer userId) {
        Optional<File> file = fileRepository.findBySubmitterId(userId);

        return file.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    public ResponseEntity<List<File>> findCVsByUserId(Integer userId) {
        List<File> files = fileRepository.findAllBySubmitterId(userId);

        if (files.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Nếu database không có display_name, dùng fileName hoặc logic tùy chỉnh
        files.forEach(file -> {
            if (file.getDisplayName() == null) {
                file.setDisplayName(file.getDisplayName()); // Fallback về fileName
                // Hoặc: file.setDisplayName("CV " + file.getId() + " - " + file.getFileName());
            }
        });

        return new ResponseEntity<>(files, HttpStatus.OK);
    }



    public ResponseEntity<File> findById(int id) {
        Optional<File> file = fileRepository.findById(id);
        return file.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<List<File>> findAll() {
        List<File> files = fileRepository.findAll();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    public ResponseEntity<File> update(int id, FileDTO fileDTO) throws BadRequestException {
        File file = this.fileRepository.findById(id).orElseThrow(()
                -> new BadRequestException());

        BeanUtils.copyProperties(fileDTO, file, BeanUtilsHelper.getNullPropertyNames(fileDTO));

        this.fileRepository.save(file);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }
    public ResponseEntity<File> DeActive(int id) throws BadRequestException {
        File file = this.fileRepository.findById(id).orElse(null);
        if (file == null) {
            throw new BadRequestException();
        }
        file.setActive(false);
        file.setDeletedAt(Timestamp.from(Instant.now().plus((Duration.ofHours(6)))));
        this.fileRepository.save(file);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }
    public ResponseEntity<String> deleteFile(int id) {
        Optional<File> file = fileRepository.findById(id);
        if (file.isEmpty()) {
            return new ResponseEntity<>("File không tồn tại.", HttpStatus.NOT_FOUND);
        }
        fileRepository.deleteById(id);
        return new ResponseEntity<>("Xóa file thành công.", HttpStatus.OK);
    }


}
