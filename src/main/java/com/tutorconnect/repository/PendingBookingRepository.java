package com.tutorconnect.repository;

import com.tutorconnect.model.PendingBooking;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PendingBookingRepository extends MongoRepository<PendingBooking, String> {
	List<PendingBooking> findByTutorIdAndStatus(String tutorId, String status);
}
