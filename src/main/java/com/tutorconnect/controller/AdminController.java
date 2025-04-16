package com.tutorconnect.controller;

import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.service.EmailService;
import com.tutorconnect.service.UserService;
import com.tutorconnect.repository.TutorApplicationRepository;
import com.tutorconnect.repository.TutorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
	 @Autowired
	    private TutorRepository tutorRepository;

    @Autowired
    private TutorApplicationRepository tutorApplicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/applications")
    public String showTutorApplications(Model model) {
        model.addAttribute("applications", tutorApplicationRepository.findAll());
        return "admin-page"; 
    }

    // Approve a tutor application
    @PostMapping("/approve/{id}")
    public String approveTutorApplication(@PathVariable String id, Model model) {
        Optional<TutorApplication> applicationOptional = tutorApplicationRepository.findById(id);

        if (applicationOptional.isPresent()) {
            TutorApplication application = applicationOptional.get();

            application.setApproved(true);
            tutorApplicationRepository.save(application);

            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

            User tutor = new User();
            tutor.setName(application.getName());
            tutor.setEmail(application.getEmail());
            tutor.setPassword(temporaryPassword); 
            tutor.setRole("tutor");
            userService.saveUser(tutor); 
            Tutors tutors = new Tutors();
            tutors.setUserId(tutor.getId());
            tutors.setName(application.getName());
            tutors.setEmail(application.getEmail());
            tutors.setQualifications(application.getQualifications());
            tutors.setBio(""); 
            tutors.setSubjects(List.of("")); 
            tutors.setHourlyRate(0.0);
            tutors.setAverageRating(0.0);
            tutors.setTotalRatings(0);
            tutors.setAvailability(new ArrayList<>());
            // Save the tutor
            tutorRepository.save(tutors);

            // Send email 
            emailService.sendPasswordResetEmail(application.getEmail(), temporaryPassword);

            
            model.addAttribute("successMessage", "Tutor application approved and email sent with a temporary password.");
        } else {
            model.addAttribute("errorMessage", "Application not found.");
        }

        return "redirect:/admin/applications";
    }

    @GetMapping("/signup")
    public String showAdminSignUpPage() {
        return "admin-signup"; 
    }

    @PostMapping("/signup")
    public String handleAdminSignUp(@RequestParam("name") String name,
                                    @RequestParam("email") String email,
                                    @RequestParam("password") String password,
                                    Model model) {
        // Create a new admin user
        User admin = new User();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPassword(password); 
        admin.setRole("admin");

        userService.saveUser(admin); 

        model.addAttribute("successMessage", "Admin signed up successfully. Please log in.");
        return "redirect:/users/signin"; 
    }
}
