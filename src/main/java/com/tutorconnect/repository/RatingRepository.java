package com.tutorconnect.repository;

import com.tutorconnect.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RatingRepository extends MongoRepository<Rating, String> {
    List<Rating> findByTutorId(String tutorId);
}
