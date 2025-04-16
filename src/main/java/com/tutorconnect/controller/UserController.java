package com.tutorconnect.controller;

import com.tutorconnect.model.ChangePasswordRequest;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.repository.UserRepository;
import com.tutorconnect.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Controller
@RequestMapping("/users")
public class UserController {
	private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/signup")
    public String showSignUpPage() {
        return "signup"; 
    }

    @PostMapping("/signup")
    public String createUser(User user, Model model) {
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already exists.");
            return "signup"; 
        }

        userService.saveUser(user);
        model.addAttribute("message", "Sign-up successful! Please sign in.");
        return "redirect:/users/signin";
    }
    

    @GetMapping("/signin")
    public String signIn(HttpServletRequest request, Model model) {
        
        return "signin"; 
    }

    @PostMapping("/signin")
    public String signIn() {
        return "redirect:/tutors/top"; 
    }


    @GetMapping("/profile")
    public String getUserProfile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("userId", user.getId());
        model.addAttribute("email", user.getEmail());
        return "profile"; 
    }


    // Change password 
    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User user,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model) {
        try {
            userService.changePassword(user.getId(), oldPassword, newPassword);

            user.setFirstLogin(false);
            model.addAttribute("message", "Password changed successfully.");
            return "signin"; 
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "change-password"; 
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password"; 
    }
    @GetMapping("/index")
    public String index() {
        return "index"; 
    }
  

}
