package com.fsoft.fintern.controllers;

import com.fsoft.fintern.services.OCR_Service;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/cv")
public class CVController {

    @Autowired
    private OCR_Service ocrService;

    @PostMapping("/save")
    public ResponseEntity<?> saveCVInfo(@RequestParam String driveLink,
                                        @RequestParam int fileId,
                                        @RequestParam int recruitmentId) {
        System.out.println("Received request: driveLink=" + driveLink + ", fileId=" + fileId + ", recruitmentId=" + recruitmentId);

        try {
            Map<String, List<String>> cvData = ocrService.processGoogleDriveLink(driveLink);
            ocrService.saveToDatabase(cvData, fileId, recruitmentId);
            return ResponseEntity.ok("CV information saved successfully.");
        } catch (IOException | InterruptedException | TesseractException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing CV: " + e.getMessage());
        }
    }

}
