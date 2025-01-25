package com.tutorconnect.controller;

import com.tutorconnect.model.User;
import com.tutorconnect.model.TutorApplication; // Add this import
import com.tutorconnect.repository.TutorApplicationRepository; // Add this import
import com.tutorconnect.service.UserService;
import com.tutorconnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.tutorconnect.model.ChangePasswordRequest; 
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository; // Inject the TutorApplicationRepository

    // Sign-up endpoint
    @PostMapping("/signup")
    public User createUser(@RequestBody User user) {
        // If the user role is "tutor", check if the application is approved
        if ("tutor".equalsIgnoreCase(user.getRole())) {
            // Check if tutor application exists
            TutorApplication application = tutorApplicationRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Tutor application not found"));

            if (!application.isApproved()) {
                throw new RuntimeException("Tutor application not approved");
            }
        }

        // Save the user after validation
        return userService.saveUser(user);
    }

    // Sign-in endpoint
    @PostMapping("/signin")
    public String signIn(@RequestBody User user) {
        User authenticatedUser = userService.authenticateUser(user);

        if (authenticatedUser != null) {
            String jwtToken = jwtUtil.generateToken(authenticatedUser.getEmail());
            return jwtToken;
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.changePassword(changePasswordRequest.getUserId(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
