package com.tutorconnect.controller;


import com.tutorconnect.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @GetMapping("/create-event")
    public String createEvent() throws IOException, GeneralSecurityException {
        // Create the event and get the link to the event
        String eventLink = googleCalendarService.createEvent();
        return "Event created successfully! You can view it here: " + eventLink;
    }
}
