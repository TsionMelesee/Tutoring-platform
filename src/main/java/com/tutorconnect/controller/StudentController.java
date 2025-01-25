package com.tutorconnect.controller;

import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.service.PendingBookingService;
import com.tutorconnect.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private TutorService tutorService;
    @Autowired
    private PendingBookingService pendingBookingService;

    // Endpoint to search tutors by subject and filter by hourly rate
    @GetMapping("/search-tutors")
    public List<Tutors> searchTutors(
            @RequestParam("subject") String subject,
            @RequestParam(value = "minHourlyRate", required = false) Double minHourlyRate,
            @RequestParam(value = "maxHourlyRate", required = false) Double maxHourlyRate) {
        return tutorService.searchTutorsBySubjectAndRate(subject, minHourlyRate, maxHourlyRate);
    }
   
}
