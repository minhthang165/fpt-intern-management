package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.FileDTO;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.dtos.ResDTO;
import com.fsoft.fintern.services.DriveService;
import com.fsoft.fintern.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/drive")
public class DriveControler {

    @Autowired
    private DriveService service;
    @Autowired
    private FileService fileService;

    @GetMapping("/upload")
    public String showUploadPage(@SessionAttribute("user") LoginUserDTO sessionUser, Model model) {
        model.addAttribute("user_id", sessionUser.getId());
        return "upload";
    }

    @PostMapping("/uploadFileToGoogleDrive")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId) throws IOException, GeneralSecurityException {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }
        String fileType = file.getContentType();
        System.out.println("Uploading file type: " + fileType + " for User ID: " + userId);
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);
        ResDTO res = service.uploadFileToDrive(tempFile, fileType);
        FileDTO fileDTO = new FileDTO();
        fileDTO.setSubmitterId(userId);

        fileDTO.setDisplayName(file.getOriginalFilename());
        fileDTO.setPath(res.getUrl());


        fileService.createFile(fileDTO);

        response.put("success", true);
        response.put("message", "Upload thành công!");
        response.put("data", res);


        return ResponseEntity.ok(response);
    }
}
