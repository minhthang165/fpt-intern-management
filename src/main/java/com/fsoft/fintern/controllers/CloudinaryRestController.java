package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.internal.Logger;

import java.util.Map;

@RestController
@RequestMapping("/cloudinary/upload")
public class CloudinaryRestController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public CloudinaryRestController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    @Operation(description = "Upload file to cloudinary and return data")
    public ResponseEntity<Map> uploadImage(@RequestParam("file") MultipartFile file){
        System.out.println("data sout: " + file.getSize());
        Map data = this.cloudinaryService.upload(file);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
    @PostMapping("uploadFile")
    @Operation(description = "Upload file to cloudinary and return data")
    public ResponseEntity<Map> uploadImage2(@RequestParam("file") MultipartFile file){
        System.out.println("data sout: " + file.getSize());
        Map data = this.cloudinaryService.upload2(file);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
