package com.tutorconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.model.User;
import com.tutorconnect.repository.TutorApplicationRepository;
import com.tutorconnect.repository.UserRepository;
import com.tutorconnect.util.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository; // Inject the repository

    // Define allowed roles
    private static final List<String> ALLOWED_ROLES = List.of("student", "tutor", "admin");

    /**
     * Save a new user with role validation and optional tutor application checks.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User saveUser(User user) {
        validateRole(user.getRole()); // Validate the role

        // Check if the user is a tutor and verify approval status
        if ("tutor".equalsIgnoreCase(user.getRole())) {
            validateTutorApproval(user.getEmail());
        }

        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Authenticate a user by validating credentials.
     *
     * @param user the user attempting to log in
     * @return the authenticated user
     */
    public User authenticateUser(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return existingUser; // Return the user if credentials match
        }

        throw new RuntimeException("Invalid credentials"); // Throw exception if credentials don't match
    }

    /**
     * Generate a JWT token for a given user's email.
     *
     * @param email the user's email
     * @return the generated JWT token
     */
    public String generateToken(String email) {
        return jwtUtil.generateToken(email); // Use JwtUtil to generate a token
    }

    /**
     * Validate the role of a user before saving.
     *
     * @param role the user's role
     */
    private void validateRole(String role) {
        if (!ALLOWED_ROLES.contains(role.toLowerCase())) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Validate that a tutor has an approved application before saving.
     *
     * @param email the tutor's email
     */
    private void validateTutorApproval(String email) {
        TutorApplication application = tutorApplicationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor application not found"));

        if (!application.isApproved()) {
            throw new RuntimeException("Tutor application not approved yet");
        }
    }

    /**
     * Retrieve all users by their role.
     *
     * @param role the role to filter users by
     * @return a list of users with the specified role
     */
    public List<User> getUsersByRole(String role) {
        validateRole(role); // Validate the role
        return userRepository.findByRole(role); // Custom query to find users by role
    }

    /**
     * Change the user's password.
     *
     * @param userId the ID of the user
     * @param oldPassword the current password
     * @param newPassword the new password
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        // Find the user by their ID
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the old password matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Encrypt the new password and update the user
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
