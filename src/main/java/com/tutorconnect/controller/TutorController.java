package com.tutorconnect.controller;

import com.tutorconnect.model.Availability;
import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Rating;
import com.tutorconnect.model.TutorApplication;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.service.PendingBookingService;
import com.tutorconnect.service.TutorApplicationService;
import com.tutorconnect.service.TutorService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    @Autowired
    private TutorApplicationService tutorApplicationService;

    @Autowired
    private TutorService tutorService;
    
    private static final Logger log = LoggerFactory.getLogger(TutorController.class);


    @Autowired
    private PendingBookingService pendingBookingService;

    // Endpoint to apply as a tutor
    @PostMapping("/apply")
    public TutorApplication applyForTutor(@RequestParam("name") String name,
                                           @RequestParam("email") String email,
                                           @RequestParam("qualifications") String qualifications,
                                           @RequestParam("file") MultipartFile file) throws Exception {
        TutorApplication application = new TutorApplication();
        application.setName(name);
        application.setEmail(email);
        application.setQualifications(qualifications);
        return tutorApplicationService.applyForTutor(application, file);
    }

    // Endpoint to update tutor profile
    @PostMapping("/update-profile")
    public ResponseEntity<Tutors> updateTutorProfile(@RequestParam("tutorId") String tutorId,
                                                     @RequestBody Tutors updatedTutorDetails) {
        try {
            Tutors updatedTutor = tutorService.updateTutorProfile(tutorId, updatedTutorDetails);
            return ResponseEntity.ok(updatedTutor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Endpoint to approve tutor application
    @PostMapping("/approve")
    public TutorApplication approveTutorApplication(@RequestParam("applicationId") String applicationId) throws Exception {
        return tutorApplicationService.approveApplication(applicationId);
    }

    // Endpoint to create a booking
    @PostMapping("/create-booking")
    public ResponseEntity<?> createBooking(
        @RequestBody PendingBooking pendingBooking,
        @RequestParam("amountPaid") double amountPaid
    ) {
        try {
            // Call the service method to create the booking
            PendingBooking newBooking = pendingBookingService.createPendingBooking(pendingBooking, amountPaid);
            
            // Return a response with HTTP status CREATED (201) and the newly created booking
            return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
        } catch (RuntimeException e) {
            // If a required field is missing or there's a business error, return BAD REQUEST (400) with the error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions and return a generic error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }


    // Endpoint to get tutor availability
    @GetMapping("/availability/{tutorId}")
    public ResponseEntity<List<Availability>> getTutorAvailability(@PathVariable String tutorId) {
        try {
            List<Availability> availability = tutorService.getTutorAvailability(tutorId);
            return ResponseEntity.ok(availability);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/{tutorId}/pending-bookings")
    public ResponseEntity<List<PendingBooking>> getPendingBookings(@PathVariable String tutorId) {
        List<PendingBooking> pendingBookings = pendingBookingService.getPendingBookingsByTutor(tutorId);
        return ResponseEntity.ok(pendingBookings);
    }

    @PostMapping("/bookings/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable String bookingId, @RequestParam String status) {
        try {
            if (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("rejected")) {
                return ResponseEntity.badRequest().body("Invalid status: " + status);
            }

            PendingBooking updatedBooking = pendingBookingService.updateBookingStatus(bookingId, status);

            // If approved, return the updated booking with the meeting link
            if ("approved".equalsIgnoreCase(status)) {
                return ResponseEntity.ok("Booking approved. Google Meet link: " + updatedBooking.getMeetingLink());
            }

            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/{tutorId}/rate")
    public String rateTutor(
        @PathVariable String tutorId,
        @RequestParam String studentId,
        @RequestParam int rating) {
        
        return tutorService.rateTutor(tutorId, studentId, rating);
    }

    @GetMapping("/{tutorId}/ratings")
    public List<Rating> getTutorRatings(@PathVariable String tutorId) {
        return tutorService.getTutorRatings(tutorId);
    }



}
