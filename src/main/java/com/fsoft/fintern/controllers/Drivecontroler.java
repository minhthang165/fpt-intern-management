    package com.fsoft.fintern.controllers;
    import com.fsoft.fintern.controllers.Res.Res;
    import com.fsoft.fintern.services.driveService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.security.GeneralSecurityException;
    @RestController
    public class Drivecontroler {

        @Autowired
        private driveService service;

        @PostMapping("/uploadToGoogleDrive")
        public Object handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, GeneralSecurityException {
            if (file.isEmpty()) {
                return "FIle is empty";
            }
            File tempFile = File.createTempFile("temp", null);
            file.transferTo(tempFile);
            Res res = service.uploadPdfToDrive(tempFile);
            System.out.println(res);
            return res;
        }
    }


