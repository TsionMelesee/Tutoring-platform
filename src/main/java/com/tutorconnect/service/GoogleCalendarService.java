package com.tutorconnect.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.CreateConferenceRequest;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Google Calendar API Java";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "credentials_2.json";

    public String createEvent() throws IOException, GeneralSecurityException {
        // Authorize user
        Credential credential = getCredentials();
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Create event details
        Event event = new Event()
                .setSummary("Test Event")
                .setLocation("Virtual")
                .setDescription("This is a test event with Google Meet integration.");

        Date startDate = new Date(); // Current time
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startDate))
                .setTimeZone("UTC");
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endDate))
                .setTimeZone("UTC");

        event.setStart(start);
        event.setEnd(end);

        // Add attendees
        EventAttendee attendee = new EventAttendee().setEmail("example@gmail.com");
        event.setAttendees(Collections.singletonList(attendee));

        // Enable Google Meet link
        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(new CreateConferenceRequest()
                        .setRequestId("randomRequestId123")); // Unique request ID
        event.setConferenceData(conferenceData);

        // Insert event
        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1) // Required for Meet links
                .execute();

        return createdEvent.getHtmlLink(); // Returns the event link
    }

    private Credential getCredentials() throws IOException, GeneralSecurityException {
        // Load OAuth 2.0 client secrets
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                GoogleClientSecrets.load(JSON_FACTORY, new FileReader(CREDENTIALS_FILE_PATH)),
                Collections.singletonList("https://www.googleapis.com/auth/calendar"))
                .setAccessType("offline")
                .build();

        // Update LocalServerReceiver to use a custom port
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(54040) // Set the specific port number you want to use
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
