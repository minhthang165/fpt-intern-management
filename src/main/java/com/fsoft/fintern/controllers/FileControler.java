package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.FileDTO;
import com.fsoft.fintern.models.File;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.repositories.FileRepository;
import com.fsoft.fintern.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("file")
public class FileControler {
        private final FileService fileService;

        public FileControler(FileService fileService) {
            this.fileService = fileService;
        }

        @GetMapping("/login/file/all")
        @Operation(description = "View all files")
        public ResponseEntity<List<File>> findAll() throws BadRequestException {
            return this.fileService.findAll();
        }
        @GetMapping("/file/path/{id}")
        @Operation(description = "Get file path by ID")
        public ResponseEntity<String> getPathByFileId(@PathVariable int id) {
            return fileService.getPathByFileId(id);
        }
        @PostMapping("/file/create")
        @Operation(description = "Create a new file")
        public ResponseEntity<File> create(@RequestBody FileDTO fileDTO) throws BadRequestException {
            return this.fileService.createFile(fileDTO);
        }

        @GetMapping("/file/{id}")
        @Operation(description = "Get file by ID")
        public ResponseEntity<File> findById(@PathVariable int id) throws BadRequestException {
            return this.fileService.findById(id);
        }


        @PatchMapping("/file/update/{id}")
        @Operation(description = "Update file by Id")
        public ResponseEntity<File> update(@PathVariable int id, @RequestBody FileDTO fileDTO) throws BadRequestException {
            return this.fileService.update(id, fileDTO);
        }

    @DeleteMapping("/file/DeActive/{id}")
    @Operation(description = "Delete file by ID")
    public ResponseEntity<File> delete(@PathVariable int id) throws BadRequestException {
        return this.fileService.DeActive(id);
    }
    @GetMapping("/user/{userId}")
    @Operation(description = "Get CV file by user ID")
    public ResponseEntity<File> findCVByUserId(@PathVariable Integer userId) {
        return fileService.findCVByUserId(userId);
    }
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<File>> findCVsByUserId(@PathVariable Integer userId) {
        return fileService.findCVsByUserId(userId);
    }
    @DeleteMapping("/delete/{id}")
    @Operation(description = "Delete file permanently by ID")
    public ResponseEntity<String> deleteFile(@PathVariable int id) {
        return fileService.deleteFile(id);
    }
}


