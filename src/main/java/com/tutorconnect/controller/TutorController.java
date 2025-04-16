package com.tutorconnect.controller;

import com.tutorconnect.model.Availability;
import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Rating;
import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.repository.TutorRepository;
import com.tutorconnect.service.PendingBookingService;
import com.tutorconnect.service.TutorApplicationService;
import com.tutorconnect.service.TutorService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/tutors")
public class TutorController {
	private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private TutorApplicationService tutorApplicationService;

    @Autowired
    private TutorService tutorService;
    
    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PendingBookingService pendingBookingService;

    // Apply as a tutor
    @PostMapping("/apply")
    public String applyForTutor(@RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("qualifications") String qualifications,
                                 @RequestParam("file") MultipartFile file,
                                 Model model) {
        try {
            TutorApplication application = new TutorApplication();
            application.setName(name);
            application.setEmail(email);
            application.setQualifications(qualifications);
            tutorApplicationService.applyForTutor(application, file);
            model.addAttribute("successMessage", "Application submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error while submitting the application: " + e.getMessage());
        }
        return "tutor-apply";
    }
    @GetMapping("/apply")
    public String showTutorApplicationForm() {
        return "tutor-apply"; 
    }


   
    @GetMapping("/tutor-profile2")
    public String getTutorProfile(Authentication authentication, Model model) {
        // Get the authenticated user
        User user = (User) authentication.getPrincipal();

        if (!"tutor".equals(user.getRole())) {
            return "error"; 
        }

        Optional<Tutors> tutorProfile = tutorRepository.findByUserId(user.getId());

        if (!tutorProfile.isPresent()) {
            model.addAttribute("error", "Tutor profile not found for the current user.");
            return "error"; 
        }

        Tutors tutor = tutorProfile.get();
        model.addAttribute("tutorName", tutor.getName());
        model.addAttribute("qualifications", tutor.getQualifications());
        model.addAttribute("bio", tutor.getBio());
        model.addAttribute("hourlyRate", tutor.getHourlyRate());
        model.addAttribute("subjects", tutor.getSubjects());
        model.addAttribute("availability", tutor.getAvailability()); 
        model.addAttribute("profilePic", tutor.getProfilePic());
        return "tutor-page";
    }

   



    // Update profile details
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Authentication authentication, Model model) {
       
        User user = (User) authentication.getPrincipal();

        if (!"tutor".equals(user.getRole())) {
            return "error"; 
        }

        Optional<Tutors> tutorProfile = tutorRepository.findByUserId(user.getId());

        if (!tutorProfile.isPresent()) {
            model.addAttribute("error", "Tutor profile not found.");
            return "error"; 
        }

        Tutors tutor = tutorProfile.get();
        model.addAttribute("tutor", tutor);
        model.addAttribute("availability", tutor.getAvailability());

        return "edit-tutor-profile"; 
    }

    //  profile update
    @PostMapping("/update-profile")
    public String updateTutorProfile(
            @ModelAttribute Tutors updatedTutor,
            @RequestParam("availabilityDays") List<String> days,
            @RequestParam("startTimes") List<String> startTimes,
            @RequestParam("endTimes") List<String> endTimes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = (User) authentication.getPrincipal();

        if (!"tutor".equals(user.getRole())) {
            return "error";
        }

        Optional<Tutors> tutorProfileOptional = tutorRepository.findByUserId(user.getId());

        if (!tutorProfileOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tutor profile not found.");
            return "redirect:/tutors/edit-profile";
        }

        Tutors tutor = tutorProfileOptional.get();
        tutor.setName(updatedTutor.getName());
        tutor.setEmail(updatedTutor.getEmail());
        tutor.setQualifications(updatedTutor.getQualifications());
        tutor.setBio(updatedTutor.getBio());
        tutor.setHourlyRate(updatedTutor.getHourlyRate());
        tutor.setSubjects(updatedTutor.getSubjects());

        List<Availability> updatedAvailability = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            Availability slot = new Availability();
            slot.setDay(days.get(i));
            slot.setStartTime(convertTo12HourFormat(startTimes.get(i))); 
            slot.setEndTime(convertTo12HourFormat(endTimes.get(i))); 
            updatedAvailability.add(slot);
        }
        tutor.setAvailability(updatedAvailability);

        tutorRepository.save(tutor);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/tutors/tutor-profile2";
    }

    private String convertTo12HourFormat(String time) {
        if (time == null || time.isEmpty()) return "";
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm"); 
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        return LocalTime.parse(time, inputFormatter).format(outputFormatter);
    }


    @GetMapping("/tutor-page")
    public String tutorPage(@AuthenticationPrincipal com.tutorconnect.model.User tutor, Model model) {
        model.addAttribute("tutorName", tutor.getName());
        return "tutor-page"; 
    }
    @PostMapping("/approve")
    public String approveTutorApplication(@RequestParam("applicationId") String applicationId, Model model) {
        try {
            TutorApplication application = tutorApplicationService.approveApplication(applicationId);
            model.addAttribute("application", application);
            model.addAttribute("successMessage", "Application approved successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error while approving application: " + e.getMessage());
        }
        return "application-details";
    }

   

    // tutor availability
    @GetMapping("/availability/{tutorId}")
    public String getTutorAvailability(@PathVariable String tutorId, Model model) {
        try {
            List<Availability> availability = tutorService.getTutorAvailability(tutorId);
            model.addAttribute("availability", availability);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error fetching availability: " + e.getMessage());
        }
        return "tutor-availability";
    }

    // pending bookings
    @GetMapping("/pending-bookings")
    public String getPendingBookingsForTutor(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<PendingBooking> bookings = pendingBookingService.getPendingBookingsForAuthenticatedTutor();
        model.addAttribute("pendingBookings", bookings);
        return "pending-bookings"; 
    }

    // approved bookings
    @GetMapping("/approved")
    public String getApprovedBookingsForTutor(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<PendingBooking> bookings = pendingBookingService.getApprovedBookingsForAuthenticatedTutor();
        model.addAttribute("approvedBookings", bookings);
        return "approved-bookings"; 
    }

    // Update booking status
    @PostMapping("/bookings/{bookingId}/status")
    public String updateBookingStatus(@PathVariable String bookingId,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String manualMeetingLink,
                                       Model model) {
        try {
            if (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("rejected")) {
                model.addAttribute("errorMessage", "Invalid status: " + status);
                return "pending-bookings";
            }

            if ("approved".equalsIgnoreCase(status) && (manualMeetingLink == null || manualMeetingLink.trim().isEmpty())) {
                model.addAttribute("errorMessage", "Meeting link is required to approve the booking.");
                return "pending-bookings";
            }

            PendingBooking updatedBooking = pendingBookingService.updateBookingStatus(bookingId, status, manualMeetingLink);

            model.addAttribute("updatedBooking", updatedBooking);
           
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "pending-bookings";
    }


    // Rate a tutor
    @PostMapping("/{tutorId}/rate")
    public String rateTutor(@PathVariable String tutorId,
                            @RequestParam int rating,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            String studentId = user.getId();  

            boolean alreadyRated = tutorService.existsByTutorIdAndStudentId(tutorId, studentId);
            
            if (alreadyRated) {
                redirectAttributes.addFlashAttribute("errorMessage", "You have already rated this tutor.");
            } else {
                String result = tutorService.rateTutor(tutorId, studentId, rating);
                redirectAttributes.addFlashAttribute("successMessage", result);
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rating tutor: " + e.getMessage());
        }

        return "redirect:/tutors/profile/" + tutorId;
    }




    // Get tutor ratings
    @GetMapping("/{tutorId}/ratings")
    public String getTutorRatings(@PathVariable String tutorId, Model model) {
        List<Rating> ratings = tutorService.getTutorRatings(tutorId);
        model.addAttribute("ratings", ratings);
        return "tutor-ratings";
    }

    // Get top tutors
    @GetMapping("/top")
    public String getTopTutors(Model model) {
        List<Tutors> topTutors = tutorService.getTopTutors();
        model.addAttribute("topTutors", topTutors);
        return "top-tutors";
    }
    
 // Fetch Tutor Profile
    @GetMapping("/profile/{tutorId}")
    public String getTutorProfile(@PathVariable String tutorId, Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        String studentId = user.getId();

        boolean alreadyRated = tutorService.existsByTutorIdAndStudentId(tutorId, studentId);
        model.addAttribute("alreadyRated", alreadyRated);

        Tutors tutor = tutorService.getTutorById(tutorId);
        model.addAttribute("tutor", tutor);

        return "tutor-profile";
    }

    @GetMapping("/profile")
    public String showTutorProfile(@AuthenticationPrincipal com.tutorconnect.model.User user, Model model) {
       
        Optional<Tutors> tutor = tutorService.getTutorByUserId(user.getId());
        
        if (tutor.isPresent()) {
            Tutors tutorDetails = tutor.get(); 
            model.addAttribute("tutorName", tutorDetails.getName());
            model.addAttribute("qualifications", tutorDetails.getQualifications());
            model.addAttribute("bio", tutorDetails.getBio());
            model.addAttribute("hourlyRate", tutorDetails.getHourlyRate());
            model.addAttribute("subjects", tutorDetails.getSubjects());
            model.addAttribute("profilePic", tutorDetails.getProfilePic());
        } else {
            model.addAttribute("tutorName", "Unknown Tutor");
            model.addAttribute("qualifications", "N/A");
            model.addAttribute("bio", "N/A");
            model.addAttribute("hourlyRate", 0.0);
            model.addAttribute("subjects", List.of("N/A"));
            model.addAttribute("profilePic", "src/main/resources/static/images/download.png");
        }
        return "tutor-page"; 
    }

    @GetMapping("/edit-profile-page")
    public String showSignUpPage() {
        return "edit-profile"; 
    }
  
    //Update profile pic
    @PostMapping("/update-profile-pic/{tutorId}")
    public String updateProfilePic(@PathVariable String tutorId, 
                                   @RequestParam("profilePic") MultipartFile file, 
                                   RedirectAttributes redirectAttributes) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
                Files.write(path, bytes);

                String profilePicUrl = "/uploads/" + file.getOriginalFilename();

                Tutors updatedTutor = tutorService.updateProfilePic(tutorId, profilePicUrl);
                if (updatedTutor != null) {
                    redirectAttributes.addFlashAttribute("successMessage", "Profile picture updated successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tutor not found!");
                }
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error uploading file!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload!");
        }
        

        return "redirect:/tutors/profile";
    }

}
