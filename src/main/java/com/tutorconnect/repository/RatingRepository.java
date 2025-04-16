package com.tutorconnect.repository;

import com.tutorconnect.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

import java.util.Optional;

public interface RatingRepository extends MongoRepository<Rating, String> {
    List<Rating> findByTutorId(String tutorId);
    Optional<Rating> findByTutorIdAndStudentId(String tutorId, String studentId);
    boolean existsByTutorIdAndStudentId(String tutorId, String studentId);
}
