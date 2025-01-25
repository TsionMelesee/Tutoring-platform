package com.tutorconnect.service;

import com.tutorconnect.model.Availability;
import com.tutorconnect.model.Rating;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.repository.RatingRepository;
import com.tutorconnect.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class TutorService {
    
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private RatingRepository ratingRepository;
    public Tutors updateTutorProfile(String tutorId, Tutors updatedTutorDetails) {
        Tutors tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));

        if (updatedTutorDetails.getBio() != null) {
            tutor.setBio(updatedTutorDetails.getBio());
        }
        if (updatedTutorDetails.getSubjects() != null) {
            tutor.setSubjects(updatedTutorDetails.getSubjects());
        }
        if (updatedTutorDetails.getHourlyRate() != null && updatedTutorDetails.getHourlyRate() > 0) {
            tutor.setHourlyRate(updatedTutorDetails.getHourlyRate());
        }
        if (updatedTutorDetails.getAvailability() != null) {
            tutor.setAvailability(updatedTutorDetails.getAvailability());
        }

        return tutorRepository.save(tutor);
    }
    public List<Tutors> searchTutorsBySubjectAndRate(String subject, Double minHourlyRate, Double maxHourlyRate) {
        if (minHourlyRate != null && maxHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingAndHourlyRateBetween(subject, minHourlyRate, maxHourlyRate);
        } else if (minHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingAndHourlyRateGreaterThanEqual(subject, minHourlyRate);
        } else if (maxHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingAndHourlyRateLessThanEqual(subject, maxHourlyRate);
        } else {
            return tutorRepository.findBySubjectsContaining(subject);
        }
        
    }
    public List<Availability> getTutorAvailability(String tutorId) {
        Tutors tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));

        if (tutor.getAvailability() == null || tutor.getAvailability().isEmpty()) {
            throw new RuntimeException("No availability schedule found for this tutor.");
        }

        return tutor.getAvailability();
    }
    public String rateTutor(String tutorId, String studentId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        // Save rating in database
        Rating newRating = new Rating(tutorId, studentId, rating);
        ratingRepository.save(newRating);

        // Update tutor's average rating
        Tutors tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));

        List<Rating> ratings = ratingRepository.findByTutorId(tutorId);
        double average = ratings.stream().mapToInt(Rating::getRating).average().orElse(0);

        tutor.setAverageRating(average);
        tutor.setTotalRatings(ratings.size());
        tutorRepository.save(tutor);

        return "Rating submitted successfully.";
    }

    public List<Rating> getTutorRatings(String tutorId) {
        return ratingRepository.findByTutorId(tutorId);
    }

}