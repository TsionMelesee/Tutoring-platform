package com.tutorconnect.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.tutorconnect.model.User;
import java.util.List;
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email); // This will help us find users by email
    List<User> findByRole(String role);
}
