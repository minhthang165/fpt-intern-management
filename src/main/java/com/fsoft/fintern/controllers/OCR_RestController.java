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
@RequestMapping("/api/ocr")
public class OCR_RestController {

    @Autowired
    private OCR_Service ocrService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, List<String>>> processDriveLink(@RequestParam("link") String driveLink) {
        try {
            Map<String, List<String>> result = ocrService.processGoogleDriveLink(driveLink);
            return ResponseEntity.ok(result);
        } catch (IOException | InterruptedException | TesseractException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
