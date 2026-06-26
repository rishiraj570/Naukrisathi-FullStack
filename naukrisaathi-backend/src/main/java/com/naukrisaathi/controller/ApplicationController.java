package com.naukrisaathi.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naukrisaathi.dto.ApplyRequest;
import com.naukrisaathi.dto.UpdateStatusRequest;
import com.naukrisaathi.model.Application;
import com.naukrisaathi.model.Internship;
import com.naukrisaathi.model.User;
import com.naukrisaathi.repository.ApplicationRepository;
import com.naukrisaathi.repository.InternshipRepository;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final InternshipRepository internshipRepository;

    public ApplicationController(ApplicationRepository applicationRepository,
                                  InternshipRepository internshipRepository) {
        this.applicationRepository = applicationRepository;
        this.internshipRepository = internshipRepository;
    }

    // POST /api/applications  — student applies
    @PostMapping
    public ResponseEntity<Map<String, Object>> apply(@RequestBody ApplyRequest req,
                                                      @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        if (!"job seeker".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Only job seekers can apply"));
        }
        if (applicationRepository.existsByUserIdAndInternshipId(user.getId(), req.getInternshipId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You have already applied for this internship"));
        }

        Optional<Internship> internshipOpt = internshipRepository.findById(req.getInternshipId());
        if (internshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Internship not found"));
        }

        Application app = new Application();
        app.setInternshipId(req.getInternshipId());
        app.setUserId(user.getId());
        app.setCoverLetter(req.getCoverLetter());
        app.setStatus("applied");
        Application saved = applicationRepository.save(app);

        // Increment applicant count
        Internship internship = internshipOpt.get();
        internship.setApplicantCount(internship.getApplicantCount() + 1);
        internshipRepository.save(internship);

        return ResponseEntity.ok(Map.of("application", saved, "message", "Applied successfully"));
    }

    // GET /api/applications/mine  — student's own applications with internship details
    @GetMapping("/mine")
    public ResponseEntity<Map<String, Object>> myApplications(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        List<Application> apps = applicationRepository.findByUserId(user.getId());
        List<Map<String, Object>> enriched = new ArrayList<>();

        for (Application app : apps) {
            Map<String, Object> item = new HashMap<>();
            item.put("_id", app.getId());
            item.put("status", app.getStatus());
            item.put("appliedAt", app.getAppliedAt());
            item.put("coverLetter", app.getCoverLetter());

            internshipRepository.findById(app.getInternshipId()).ifPresent(internship -> {
                Map<String, Object> inMap = new HashMap<>();
                inMap.put("_id", internship.getId());
                inMap.put("title", internship.getTitle());
                inMap.put("company", internship.getCompany());
                inMap.put("companyColor", internship.getCompanyColor());
                inMap.put("location", internship.getLocation());
                inMap.put("type", internship.getType());
                inMap.put("stipend", internship.getStipend());
                inMap.put("duration", internship.getDuration());
                item.put("internship", inMap);
            });
            enriched.add(item);
        }

        return ResponseEntity.ok(Map.of("applications", enriched));
    }

    // GET /api/applications/internship/:internshipId  — recruiter sees applicants
    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<Map<String, Object>> forInternship(@PathVariable String internshipId,
                                                              @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        List<Application> apps = applicationRepository.findByInternshipId(internshipId);
        return ResponseEntity.ok(Map.of("applications", apps));
    }

    // PUT /api/applications/:id/status  — recruiter updates status
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable String id,
                                                             @RequestBody UpdateStatusRequest req,
                                                             @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        return applicationRepository.findById(id).map(app -> {
            app.setStatus(req.getStatus());
            app.setUpdatedAt(LocalDateTime.now());
            applicationRepository.save(app);
            return ResponseEntity.ok(Map.of("message", "Status updated", "application", app));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/applications/:id  — student withdraws
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> withdraw(@PathVariable String id,
                                                         @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Optional<Application> appOpt = applicationRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Application not found"));
        }

        Application app = appOpt.get();
        if (!app.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        applicationRepository.deleteById(id);

        // Decrement applicant count
        internshipRepository.findById(app.getInternshipId()).ifPresent(internship -> {
            int count = Math.max(0, internship.getApplicantCount() - 1);
            internship.setApplicantCount(count);
            internshipRepository.save(internship);
        });

        return ResponseEntity.ok(Map.of("message", "Application withdrawn"));
    }
}
