package com.tutorconnect.service;

import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.repository.TutorApplicationRepository;
import com.tutorconnect.repository.TutorRepository; 
import com.tutorconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Add BCryptPasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class TutorApplicationService {

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository; // Repository to save tutor application details

    @Autowired
    private TutorRepository tutorRepository; // Repository to save tutor details

    @Autowired
    private UserRepository userRepository; // Repository to update user details (if needed)

    @Autowired
    private EmailService emailService; // Service to send emails (for password reset)
    
    @Autowired
    private GoogleDriveService googleDriveService; // GoogleDrive Service for file handling

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // BCryptPasswordEncoder to hash passwords
    
    public TutorApplication applyForTutor(TutorApplication application, MultipartFile file) throws Exception {
        // Handle uploading the file and save the application
        String fileUrl = googleDriveService.uploadFile(file); 
        application.setDocumentPath(fileUrl);
        
        return tutorApplicationRepository.save(application);
    }

    public TutorApplication approveApplication(String applicationId) throws Exception {
        // Retrieve the application
        TutorApplication application = tutorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new Exception("Application not found"));

        // Approve the application
        application.setApproved(true);
        tutorApplicationRepository.save(application);

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // Create a tutor object
        Tutors tutor = new Tutors();
        tutor.setName(application.getName());
        tutor.setEmail(application.getEmail());
        tutor.setQualifications(application.getQualifications());
        tutor.setPassword(encodedPassword);

        // Debugging: Log before saving
        System.out.println("Attempting to save tutor: " + tutor.getName());

        // Save tutor to repository
        Tutors savedTutor = tutorRepository.save(tutor);

        // Debugging: Log after saving
        System.out.println("Tutor saved successfully with ID: " + savedTutor.getId());

        if (savedTutor.getId() == null) {
            throw new Exception("Tutor save failed. Check database connection and logs.");
        }

        // Create corresponding User object
        User user = new User();
        user.setName(application.getName());
        user.setEmail(application.getEmail());
        user.setRole("tutor");
        user.setPassword(encodedPassword);

        // Save user to repository
        userRepository.save(user);

        // Send email notification
        String subject = "Tutor Application Approved";
        String body = "Dear " + application.getName() + ",\n\n" +
                      "Your application has been approved. Use the temporary password: " + temporaryPassword;

        emailService.sendEmail(application.getEmail(), subject, body);

        return application;
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8); // Generate a temporary password (8 characters long)
    }
}
