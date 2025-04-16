package com.tutorconnect.repository;

import com.tutorconnect.model.TutorApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TutorApplicationRepository extends MongoRepository<TutorApplication, String> {
    Optional<TutorApplication> findByEmail(String email);  
}
