package com.tutorconnect.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service 
public class GoogleDriveService {

    private static final String CREDENTIALS_FILE_PATH = "tutoring-platform-446113-587aeef05c30.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private Drive getDriveService() throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(new java.io.FileInputStream(CREDENTIALS_FILE_PATH))
                .createScoped(SCOPES);

        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName("TutorConnect")
                .build();
    }

    public String uploadFile(MultipartFile file) throws IOException, GeneralSecurityException {
        Drive driveService = getDriveService();

        InputStream fileInputStream = file.getInputStream();

        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());

        InputStreamContent mediaContent = new InputStreamContent(file.getContentType(), fileInputStream);

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return "https://drive.google.com/file/d/" + uploadedFile.getId() + "/view";
    }
}
