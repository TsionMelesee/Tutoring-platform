package com.tutorconnect.service;

import com.tutorconnect.model.Availability;
import com.tutorconnect.model.Rating;
import com.tutorconnect.model.Tutors;
import com.tutorconnect.repository.RatingRepository;
import com.tutorconnect.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class TutorService {
    
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private RatingRepository ratingRepository;
    
    public List<Tutors> searchTutorsBySubjectAndRate(String subject, Double minHourlyRate, Double maxHourlyRate) {
        if (subject == null || subject.isEmpty()) {
            return tutorRepository.findAll(); 
        }

        if (minHourlyRate != null && maxHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingIgnoreCaseAndHourlyRateBetween(subject, minHourlyRate, maxHourlyRate);
        } else if (minHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingIgnoreCaseAndHourlyRateGreaterThanEqual(subject, minHourlyRate);
        } else if (maxHourlyRate != null) {
            return tutorRepository.findBySubjectsContainingIgnoreCaseAndHourlyRateLessThanEqual(subject, maxHourlyRate);
        } else {
            return tutorRepository.findBySubjectsContainingIgnoreCase(subject);
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

        if (ratingRepository.findByTutorIdAndStudentId(tutorId, studentId).isPresent()) {
            throw new RuntimeException("You have already rated this tutor.");
        }

        Rating newRating = new Rating(tutorId, studentId, rating);
        ratingRepository.save(newRating);

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
    public List<Tutors> getTopTutors() {
        return tutorRepository.findAll().stream()
            .sorted(Comparator.comparing(Tutors::getAverageRating).reversed()
                .thenComparing(Tutors::getName))
            .limit(6)
            .collect(Collectors.toList());
    }
    public Tutors getTutorProfile(String tutorId) {
        return tutorRepository.findById(tutorId)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
    public Optional<Tutors> getTutorByUserId(String userId) {
        return tutorRepository.findByUserId(userId);
    }

    public Tutors updateTutorProfile(String userId, Tutors updatedTutorData) {
        Optional<Tutors> existingTutorOpt = tutorRepository.findByUserId(userId);
        if (existingTutorOpt.isPresent()) {
            Tutors existingTutor = existingTutorOpt.get();
            existingTutor.setBio(updatedTutorData.getBio());
            existingTutor.setQualifications(updatedTutorData.getQualifications());
            existingTutor.setHourlyRate(updatedTutorData.getHourlyRate());
            existingTutor.setSubjects(updatedTutorData.getSubjects());
            existingTutor.setAvailability(updatedTutorData.getAvailability());
            return tutorRepository.save(existingTutor);
        } else {
            throw new IllegalArgumentException("Tutor not found for userId: " + userId);
        }
    }
    public void saveTutor(Tutors tutor) {
        tutorRepository.save(tutor);
    }
    public Tutors getTutorById(String tutorId) {
        return tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
    
    public boolean existsByTutorIdAndStudentId(String tutorId, String studentId) {
        return ratingRepository.existsByTutorIdAndStudentId(tutorId, studentId);
    }
    public Tutors updateProfilePic(String tutorId, String profilePicUrl) {
        Optional<Tutors> optionalTutor = tutorRepository.findById(tutorId);
        if (optionalTutor.isPresent()) {
            Tutors tutor = optionalTutor.get();
            tutor.setProfilePic(profilePicUrl);
            return tutorRepository.save(tutor);
        }
        return null;
    }




}