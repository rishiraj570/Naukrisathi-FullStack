package com.naukrisaathi.controller;

import com.naukrisaathi.model.Internship;
import com.naukrisaathi.model.User;
import com.naukrisaathi.repository.InternshipRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/internships")
public class InternshipController {

    private final InternshipRepository internshipRepository;
    private final MongoTemplate mongoTemplate;

    public InternshipController(InternshipRepository internshipRepository, MongoTemplate mongoTemplate) {
        this.internshipRepository = internshipRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // GET /api/internships/featured
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeatured() {
        List<Internship> featured = internshipRepository.findByFeaturedTrueAndActiveTrue();
        // If no featured, return all active (good for fresh DBs)
        if (featured.isEmpty()) {
            featured = internshipRepository.findByActiveTrue();
        }
        return ResponseEntity.ok(Map.of("internships", featured));
    }

    // GET /api/internships?keyword=&city=&type=&minPay=&maxPay=
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minPay,
            @RequestParam(required = false) Integer maxPay) {

        Query query = new Query();
        query.addCriteria(Criteria.where("active").is(true));

        if (keyword != null && !keyword.isBlank()) {
            Criteria keywordCriteria = new Criteria().orOperator(
                Criteria.where("title").regex(keyword, "i"),
                Criteria.where("company").regex(keyword, "i"),
                Criteria.where("skills").regex(keyword, "i")
            );
            query.addCriteria(keywordCriteria);
        }
        if (city != null && !city.isBlank()) {
            query.addCriteria(Criteria.where("location").regex(city, "i"));
        }
        if (type != null && !type.isBlank()) {
            query.addCriteria(Criteria.where("type").regex(type, "i"));
        }
        if (minPay != null) {
            query.addCriteria(Criteria.where("stipend").gte(minPay));
        }
        if (maxPay != null) {
            query.addCriteria(Criteria.where("stipend").lte(maxPay));
        }

        List<Internship> results = mongoTemplate.find(query, Internship.class);
        return ResponseEntity.ok(Map.of("internships", results));
    }

    // GET /api/internships/:id
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return internshipRepository.findById(id)
                .map(i -> ResponseEntity.ok((Object) i))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/internships  (recruiter only)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Internship internship,
                                    @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        if (!"recruiter".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Only recruiters can post internships"));
        }
        internship.setPostedBy(user.getId());
        internship.setActive(true);
        Internship saved = internshipRepository.save(internship);
        return ResponseEntity.ok(Map.of("internship", saved, "message", "Internship created successfully"));
    }

    // PUT /api/internships/:id
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody Internship updates,
                                    @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        return internshipRepository.findById(id).map(existing -> {
            if (!existing.getPostedBy().equals(user.getId()) && !"recruiter".equals(user.getRole())) {
                return ResponseEntity.status(403).body((Object) Map.of("message", "Forbidden"));
            }
            if (updates.getTitle() != null)    existing.setTitle(updates.getTitle());
            if (updates.getCompany() != null)  existing.setCompany(updates.getCompany());
            if (updates.getLocation() != null) existing.setLocation(updates.getLocation());
            if (updates.getType() != null)     existing.setType(updates.getType());
            if (updates.getStipend() != null)  existing.setStipend(updates.getStipend());
            if (updates.getDuration() != null) existing.setDuration(updates.getDuration());
            if (updates.getSkills() != null)   existing.setSkills(updates.getSkills());
            if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
            Internship saved = internshipRepository.save(existing);
            return ResponseEntity.ok((Object) Map.of("internship", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/internships/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id,
                                    @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        return internshipRepository.findById(id).map(existing -> {
            internshipRepository.deleteById(id);
            return ResponseEntity.ok((Object) Map.of("message", "Deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
