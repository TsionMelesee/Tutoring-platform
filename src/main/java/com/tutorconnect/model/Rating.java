package com.tutorconnect.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ratings")
public class Rating {
    @Id
    private String id;
    private String tutorId;
    private String studentId;
    private int rating; // Value from 1 to 5

    // Constructor
    public Rating(String tutorId, String studentId, int rating) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.rating = rating;
    }

    // Getters and setters
    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
