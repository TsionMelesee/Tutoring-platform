package com.tutorconnect.service;

import com.tutorconnect.model.PendingBooking;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.model.User;
import com.tutorconnect.repository.PendingBookingRepository;
import com.tutorconnect.repository.TutorRepository;
import com.tutorconnect.repository.UserRepository;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
    
    private static final String CREDENTIALS_FILE_PATH = "credentials_2.json";  // Direct path to credentials file
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    public PendingBooking createPendingBooking(PendingBooking pendingBooking, double amountPaid) {
        // Fetch the student details using their ID
        User student = userRepository.findById(pendingBooking.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + pendingBooking.getStudentId()));

        // Fetch the tutor details using their ID
        Tutors tutor = tutorsRepository.findById(pendingBooking.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found with ID: " + pendingBooking.getTutorId()));

        // Populate the missing fields in the PendingBooking object
        pendingBooking.setStudentName(student.getName());
        pendingBooking.setStudentEmail(student.getEmail());
        pendingBooking.setTutorName(tutor.getName());
        pendingBooking.setTutorEmail(tutor.getEmail());

        // Set status to 'paying' to indicate payment process started
        pendingBooking.setStatus("paying");

        // Record the payment amount
        pendingBooking.setAmountPaid(amountPaid);

        // Set initial paid status as false (payment is initiated but not completed)
        pendingBooking.setPaid(false);

        // Ensure selectedTime is not null or empty before saving
        if (pendingBooking.getSelectedTime() == null || pendingBooking.getSelectedTime().isEmpty()) {
            throw new RuntimeException("Selected time is required");
        }

        // Ensure subject is not null or empty before saving
        if (pendingBooking.getSubject() == null || pendingBooking.getSubject().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        // Save the pending booking to the repository
        return pendingBookingRepository.save(pendingBooking);
    }

    public List<PendingBooking> getPendingBookingsByTutor(String tutorId) {
        return pendingBookingRepository.findByTutorIdAndStatus(tutorId, "pending");
    }

    public PendingBooking updateBookingStatus(String bookingId, String status) {
        var booking = pendingBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);

        if ("approved".equalsIgnoreCase(status)) {
            try {
                if (booking.getSelectedTime() == null || booking.getSelectedTime().trim().isEmpty()) {
                    throw new RuntimeException("Selected time is missing for booking ID: " + booking.getId());
                }

                String meetingLink = createGoogleMeetLink(booking);
                booking.setMeetingLink(meetingLink);

                sendEmails(booking, meetingLink);
                booking.setPaid(true); 
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate Google Meet link: " + e.getMessage());
            }
        }

        return pendingBookingRepository.save(booking);
    }
    private Credential getCredentials() throws IOException, GeneralSecurityException {
        // Load OAuth 2.0 client secrets
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                GoogleClientSecrets.load(JSON_FACTORY, new FileReader(CREDENTIALS_FILE_PATH)),
                Collections.singletonList("https://www.googleapis.com/auth/calendar.events"))
                .setAccessType("offline")
                .build();

        // Use a fixed port for the redirect URI
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(54040) // Use a specific, consistent port
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private String createGoogleMeetLink(PendingBooking booking) throws IOException, GeneralSecurityException {
        // Authorize user
        Credential credential = getCredentials();
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .build();

        // Parse selected time from booking
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
        LocalDateTime startDateTime = LocalDateTime.parse(booking.getSelectedTime(), formatter);
        LocalDateTime endDateTime = startDateTime.plusHours(1); // Add 1 hour to start time

        // Convert start and end times to DateTime format for Google Calendar API
        DateTime start = new DateTime(java.sql.Timestamp.valueOf(startDateTime));
        DateTime end = new DateTime(java.sql.Timestamp.valueOf(endDateTime));

        // Create event details
        Event event = new Event()
                .setSummary("Tutoring Session with " + booking.getStudentName())
                .setLocation("Google Meet")
                .setDescription("Tutoring session between " + booking.getTutorName() + " and " + booking.getStudentName())
                .setStart(new EventDateTime().setDateTime(start).setTimeZone("UTC"))
                .setEnd(new EventDateTime().setDateTime(end).setTimeZone("UTC"));

        // Add attendees: student email
        EventAttendee attendee = new EventAttendee().setEmail(booking.getStudentEmail());
        event.setAttendees(Collections.singletonList(attendee));

        // Enable Google Meet link
        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(new CreateConferenceRequest().setRequestId(UUID.randomUUID().toString()));
        event.setConferenceData(conferenceData);

        // Insert the event into the tutor's calendar
        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1) // Required for Meet links
                .execute();

        return createdEvent.getHangoutLink(); // Returns the Google Meet link
    }


   

    private void sendEmails(PendingBooking booking, String meetingLink) {
        String studentEmailContent = "Dear " + booking.getStudentName() + ",\n\n" +
                "Your booking with " + booking.getTutorName() + " is approved.\n" +
                "Join the session using this link: " + meetingLink + "\n\nBest regards,\nTutorConnect Team";
        emailService.sendEmail(booking.getStudentEmail(), "Booking Approved", studentEmailContent);

        String tutorEmailContent = "Dear " + booking.getTutorName() + ",\n\n" +
                "A booking with " + booking.getStudentName() + " is approved.\n" +
                "Here is the Google Meet link: " + meetingLink + "\n\nBest regards,\nTutorConnect Team";
        emailService.sendEmail(booking.getTutorEmail(), "Booking Approved", tutorEmailContent);
    }

}
