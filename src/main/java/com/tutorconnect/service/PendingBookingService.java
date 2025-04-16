package com.tutorconnect.service;

import com.tutorconnect.model.Availability;
import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.repository.PendingBookingRepository;
import com.tutorconnect.repository.TutorRepository;
import com.tutorconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PendingBookingService {

    @Autowired
    private PendingBookingRepository pendingBookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorsRepository;

    @Autowired
    private EmailService emailService;

    public PendingBooking createPendingBooking(PendingBooking pendingBooking) {
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User authenticatedUser = userRepository.findByEmail(loggedInUserEmail);
        if (authenticatedUser == null) {
            throw new RuntimeException("Logged-in user not found");
        }

        Tutors tutor = tutorsRepository.findById(pendingBooking.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found with ID: " + pendingBooking.getTutorId()));

        pendingBooking.setStudentId(authenticatedUser.getId());
        pendingBooking.setStudentName(authenticatedUser.getName());
        pendingBooking.setStudentEmail(authenticatedUser.getEmail());
        pendingBooking.setTutorName(tutor.getName());
        pendingBooking.setTutorEmail(tutor.getEmail());
        pendingBooking.setStatus("paying");
        pendingBooking.setPaid(false);

        if (pendingBooking.getSelectedTime() == null || pendingBooking.getSelectedTime().isEmpty()) {
            throw new RuntimeException("Selected time is required");
        }
        if (pendingBooking.getSubject() == null || pendingBooking.getSubject().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
            LocalDateTime startDateTime = LocalDateTime.parse(pendingBooking.getSelectedTime().trim(), formatter);

           
            LocalDateTime endDateTime = startDateTime.plusHours(1);

            double amountPaid = tutor.getHourlyRate();
            pendingBooking.setAmountPaid(amountPaid);

            System.out.println("Booking Start Time: " + startDateTime);
            System.out.println("Booking End Time: " + endDateTime);

        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time format. Expected format: 'yyyy-MM-dd hh:mm a'", e);
        }

        return pendingBookingRepository.save(pendingBooking);
    }



    public List<PendingBooking> getPendingBookingsByTutor(String tutorId) {
        return pendingBookingRepository.findByTutorIdAndStatus(tutorId, "pending");
    }
    public List<PendingBooking> getPendingBookingsForTutor(String tutorId) {
        return pendingBookingRepository.findByTutorIdAndStatus(tutorId, "pending");
    }

    public List<PendingBooking> getApprovedBookingsForTutor(String tutorId) {
        return pendingBookingRepository.findByTutorIdAndStatus(tutorId, "approved");
    }

    public PendingBooking updateBookingStatus(String bookingId, String status, String manualMeetingLink) {
        var booking = pendingBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);

        if ("approved".equalsIgnoreCase(status)) {
            if (manualMeetingLink == null || manualMeetingLink.trim().isEmpty()) {
                throw new RuntimeException("Meeting link is required for approval");
            }

            booking.setMeetingLink(manualMeetingLink);
            sendEmails(booking, manualMeetingLink);
            booking.setPaid(true);
            removeSelectedTimeFromTutorAvailability(booking.getTutorId(), booking.getSelectedTime());
        }

        return pendingBookingRepository.save(booking);
    }

    private void removeSelectedTimeFromTutorAvailability(String tutorId, String selectedTime) {
        Tutors tutor = tutorsRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found with ID: " + tutorId));

        tutor.getAvailability().removeIf(avail -> {
            String availabilityTime = avail.getStartTime() + " - " + avail.getEndTime();
            return availabilityTime.equals(selectedTime);
        });

        tutorsRepository.save(tutor);
    }

    private void sendEmails(PendingBooking booking, String meetingLink) {
        String studentEmailContent = "Dear " + booking.getStudentName() + ",\n\n" +
                "Your booking with " + booking.getTutorName() + " is approved.\n" +
                "Join the session using this link on " + booking.getSelectedTime() + ": " + meetingLink + "\n\nBest regards,\nTutorConnect Team";
        emailService.sendEmail(booking.getStudentEmail(), "Booking Approved", studentEmailContent);

        String tutorEmailContent = "Dear " + booking.getTutorName() + ",\n\n" +
                "A booking with " + booking.getStudentName() + " is approved.\n" +
                "Here is the Google Meet link for the session on " + booking.getSelectedTime() + ": " + meetingLink + "\n\nBest regards,\nTutorConnect Team";
        emailService.sendEmail(booking.getTutorEmail(), "Booking Approved", tutorEmailContent);
    }
    public List<PendingBooking> getPendingBookingsForAuthenticatedTutor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Optional<Tutors> tutorProfileOptional = tutorsRepository.findByUserId(user.getId());
        if (tutorProfileOptional.isEmpty()) {
            throw new RuntimeException("Tutor profile not found for authenticated user");
        }

        String tutorId = tutorProfileOptional.get().getId();
        List<PendingBooking> pendingBookings = pendingBookingRepository.findByTutorIdAndStatus(tutorId, "paying");
        
        for (PendingBooking booking : pendingBookings) {
            Optional<User> studentOptional = userRepository.findById(booking.getStudentId());
            studentOptional.ifPresent(student -> {
                booking.setStudentName(student.getName());
                booking.setStudentEmail(student.getEmail());
            });
        }
        
        return pendingBookings;
    }
    public List<PendingBooking> getApprovedBookingsForAuthenticatedTutor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Optional<Tutors> tutorProfileOptional = tutorsRepository.findByUserId(user.getId());
        if (tutorProfileOptional.isEmpty()) {
            throw new RuntimeException("Tutor profile not found for authenticated user");
        }

        String tutorId = tutorProfileOptional.get().getId();
        List<PendingBooking> approvedBookings = pendingBookingRepository.findByTutorIdAndStatus(tutorId, "approved");
        
        for (PendingBooking booking : approvedBookings) {
            Optional<User> studentOptional = userRepository.findById(booking.getStudentId());
            studentOptional.ifPresent(student -> {
                booking.setStudentName(student.getName());
                booking.setStudentEmail(student.getEmail());
            });
        }
        
        return approvedBookings;
    }
}
