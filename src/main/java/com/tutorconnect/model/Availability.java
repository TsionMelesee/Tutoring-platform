package com.tutorconnect.model;

public class Availability {
    private String day; // e.g "Monday"
    private String startTime; // e.g "10:00 AM"
    private String endTime; // e.g "12:00 PM"

    // Getters and setters
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
