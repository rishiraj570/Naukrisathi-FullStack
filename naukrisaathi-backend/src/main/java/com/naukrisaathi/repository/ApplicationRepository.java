package com.naukrisaathi.repository;

import com.naukrisaathi.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    List<Application> findByUserId(String userId);
    List<Application> findByInternshipId(String internshipId);
    Optional<Application> findByUserIdAndInternshipId(String userId, String internshipId);
    boolean existsByUserIdAndInternshipId(String userId, String internshipId);
    long countByInternshipId(String internshipId);
}
