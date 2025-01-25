package com.tutorconnect.repository;

import com.tutorconnect.model.Tutors;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TutorRepository extends MongoRepository<Tutors, String> {
    
    // Find tutors by subject
    List<Tutors> findBySubjectsContaining(String subject);

    // Find tutors by subject and filter by hourly rate range
    List<Tutors> findBySubjectsContainingAndHourlyRateBetween(String subject, Double minHourlyRate, Double maxHourlyRate);

    // Find tutors by subject and hourly rate greater than or equal to the minimum rate
    List<Tutors> findBySubjectsContainingAndHourlyRateGreaterThanEqual(String subject, Double minHourlyRate);

    // Find tutors by subject and hourly rate less than or equal to the maximum rate
    List<Tutors> findBySubjectsContainingAndHourlyRateLessThanEqual(String subject, Double maxHourlyRate);
}
