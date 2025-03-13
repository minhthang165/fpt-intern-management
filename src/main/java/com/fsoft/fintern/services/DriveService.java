package com.fsoft.fintern.services;


import com.fsoft.fintern.dtos.FileDTO;
import com.fsoft.fintern.dtos.ResDTO;
import com.fsoft.fintern.repositories.FileRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import jakarta.activation.FileDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

@org.springframework.stereotype.Service
public class DriveService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACOUNT_KEY_PATH = getPathToGoodleCredentials();

    public DriveService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "src", "main", "ggDrive", "cred.json");
        return filePath.toString();
    }

    public ResDTO uploadFileToDrive(File file, String fileType) throws GeneralSecurityException, IOException {
        ResDTO res = new ResDTO();

        try {
            String folderId = "1yasyBmXD6xN_zwqtuBuU5z0JrQVczG3o";
            Drive drive = createDriveService();

            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));

            String mimeType = fileType != null ? fileType : "application/octet-stream";
            FileContent mediaContent = new FileContent(mimeType, file);

            com.google.api.services.drive.model.File uploadedFile = drive.files()
                    .create(fileMetaData, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            String url = "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
            System.out.println("Uploaded File URL: " + url);
            file.delete();

            res.setStatus(200);
            res.setMessage("File uploaded successfully!");
            res.setUrl(url);

        } catch (Exception e) {
            System.out.println("Upload error: " + e.getMessage());
            res.setStatus(500);
            res.setMessage(e.getMessage());
        }

        return res;

    }




    private Drive createDriveService() throws GeneralSecurityException, IOException {

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .build();

    }
}
