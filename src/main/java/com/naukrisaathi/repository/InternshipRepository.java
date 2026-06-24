package com.naukrisaathi.repository;

import com.naukrisaathi.model.Internship;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InternshipRepository extends MongoRepository<Internship, String> {
    List<Internship> findByFeaturedTrueAndActiveTrue();
    List<Internship> findByActiveTrue();
    List<Internship> findByIdIn(List<String> ids);
}
