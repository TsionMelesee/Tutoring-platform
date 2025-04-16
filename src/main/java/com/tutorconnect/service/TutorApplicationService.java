package com.tutorconnect.service;

import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.repository.TutorApplicationRepository;
import com.tutorconnect.repository.TutorRepository;
import com.tutorconnect.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional; // Add this for transaction management

import java.util.UUID;

@Service
public class TutorApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(TutorApplicationService.class);

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GoogleDriveService googleDriveService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public TutorApplication applyForTutor(TutorApplication application, MultipartFile file) throws Exception {
        String fileUrl = googleDriveService.uploadFile(file);
        application.setDocumentPath(fileUrl);
        return tutorApplicationRepository.save(application);
    }

    @Transactional 
    public TutorApplication approveApplication(String applicationId) throws Exception {
        logger.info("Starting tutor approval for application ID: {}", applicationId);

        TutorApplication application = tutorApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new Exception("Application not found"));
        logger.info("Application retrieved: {}", application.getEmail());

        application.setApproved(true);
        tutorApplicationRepository.save(application);
        logger.info("Application marked as approved and saved: {}", application.getId());

        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        Tutors tutor = new Tutors();
        tutor.setName(application.getName());
        tutor.setEmail(application.getEmail());
        tutor.setQualifications(application.getQualifications());
        tutor.setBio("Welcome! Please update your bio.");
        tutor.setHourlyRate(0.0);
        tutor.setAverageRating(0.0);
        tutor.setTotalRatings(0);

        Tutors savedTutor = tutorRepository.save(tutor);
        if (savedTutor == null || savedTutor.getId() == null) {
            throw new Exception("Tutor save failed");
        }
        logger.info("Tutor successfully saved with ID: {}", savedTutor.getId());

        User user = new User();
        user.setName(application.getName());
        user.setEmail(application.getEmail());
        user.setRole("tutor");
        user.setPassword(encodedPassword);

        userRepository.save(user);
        logger.info("User successfully saved with email: {}", user.getEmail());

        String subject = "Tutor Application Approved";
        String body = String.format(
            "Dear %s,\n\nYour application has been approved. Your temporary password is: %s\n\n" +
            "Please log in and update your profile details.\n\nThank you!",
            application.getName(), temporaryPassword
        );
        emailService.sendEmail(application.getEmail(), subject, body);
        logger.info("Approval email sent to: {}", application.getEmail());

        return application;
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
