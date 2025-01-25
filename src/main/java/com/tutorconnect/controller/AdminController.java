package com.tutorconnect.controller;

import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.model.User;
import com.tutorconnect.service.EmailService;
import com.tutorconnect.service.UserService;
import com.tutorconnect.repository.TutorApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;  // Inject UserService

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveTutorApplication(@PathVariable String id) {
        Optional<TutorApplication> applicationOptional = tutorApplicationRepository.findById(id);

        if (applicationOptional.isPresent()) {
            TutorApplication application = applicationOptional.get();

            // Approve the application
            application.setApproved(true);
            tutorApplicationRepository.save(application);

            // Generate a temporary password
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

            // Create a new tutor user
            User tutor = new User();
            tutor.setName(application.getName());
            tutor.setEmail(application.getEmail());
            tutor.setPassword(temporaryPassword); // Store hashed password
            tutor.setRole("tutor"); // Set role as tutor
            userService.saveUser(tutor); // Save the user in MongoDB

            // Send email with the temporary password
            emailService.sendPasswordResetEmail(application.getEmail(), temporaryPassword);

            return ResponseEntity.ok("Tutor application approved, and email sent with a temporary password.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found.");
        }
    }

}
