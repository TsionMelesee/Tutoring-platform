package com.tutorconnect.controller;

import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.service.PendingBookingService;
import com.tutorconnect.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private TutorService tutorService;
    @Autowired
    private PendingBookingService pendingBookingService;


    @GetMapping("/search")
    public String searchTutors(@RequestParam(value = "query", required = false) String query, Model model) {
        List<Tutors> tutors = tutorService.searchTutorsBySubjectAndRate(query, null, null);
        model.addAttribute("searchResults", tutors);
        model.addAttribute("query", query);  
        return "top-tutors"; 
    }
    @PostMapping("/create-booking")
    public String createBookings(@ModelAttribute PendingBooking booking, RedirectAttributes redirectAttributes) {
        pendingBookingService.createPendingBooking(booking);
        redirectAttributes.addFlashAttribute("bookingSuccess", true);
        return "redirect:/tutors/top"; 
    }


}
