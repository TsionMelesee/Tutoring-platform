package com.tutorconnect.repository;

import com.tutorconnect.model.Tutors;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Query;

public interface TutorRepository extends MongoRepository<Tutors, String> {
    
    @Query("{ 'subjects': { $regex: ?0, $options: 'i' } }")
    List<Tutors> findBySubjectsContainingIgnoreCase(String subject);

    @Query("{ 'subjects': { $regex: ?0, $options: 'i' }, 'hourlyRate': { $gte: ?1, $lte: ?2 } }")
    List<Tutors> findBySubjectsContainingIgnoreCaseAndHourlyRateBetween(String subject, Double minHourlyRate, Double maxHourlyRate);

    @Query("{ 'subjects': { $regex: ?0, $options: 'i' }, 'hourlyRate': { $gte: ?1 } }")
    List<Tutors> findBySubjectsContainingIgnoreCaseAndHourlyRateGreaterThanEqual(String subject, Double minHourlyRate);

    @Query("{ 'subjects': { $regex: ?0, $options: 'i' }, 'hourlyRate': { $lte: ?1 } }")
    List<Tutors> findBySubjectsContainingIgnoreCaseAndHourlyRateLessThanEqual(String subject, Double maxHourlyRate);

    Optional<Tutors> findByUserId(String userId); 
}
