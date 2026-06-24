package com.naukrisaathi.controller;

import com.naukrisaathi.dto.UpdateProfileRequest;
import com.naukrisaathi.model.Internship;
import com.naukrisaathi.model.User;
import com.naukrisaathi.repository.ApplicationRepository;
import com.naukrisaathi.repository.InternshipRepository;
import com.naukrisaathi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;

    public UserController(UserRepository userRepository,
                           InternshipRepository internshipRepository,
                           ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
    }

    // GET /api/users/profile
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        return ResponseEntity.ok(Map.of("user", sanitizeUser(user)));
    }

    // PUT /api/users/profile
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UpdateProfileRequest req,
                                                              @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        if (req.getFirstName() != null)     user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)      user.setLastName(req.getLastName());
        if (req.getPhone() != null)         user.setPhone(req.getPhone());
        if (req.getCollege() != null)       user.setCollege(req.getCollege());
        if (req.getDegree() != null)        user.setDegree(req.getDegree());
        if (req.getGraduationYear() != null) user.setGraduationYear(req.getGraduationYear());
        if (req.getSkills() != null)        user.setSkills(req.getSkills());
        if (req.getBio() != null)           user.setBio(req.getBio());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of("user", sanitizeUser(saved), "message", "Profile updated"));
    }

    // POST /api/users/save/:id  — toggle save internship
    @PostMapping("/save/{internshipId}")
    public ResponseEntity<Map<String, Object>> toggleSave(@PathVariable String internshipId,
                                                           @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        List<String> saved = user.getSavedInternships();
        if (saved == null) saved = new ArrayList<>();

        String msg;
        if (saved.contains(internshipId)) {
            saved.remove(internshipId);
            msg = "Removed from saved";
        } else {
            saved.add(internshipId);
            msg = "Saved successfully";
        }
        user.setSavedInternships(saved);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", msg, "saved", saved));
    }

    // GET /api/users/saved  — list saved internships with full details
    @GetMapping("/saved")
    public ResponseEntity<Map<String, Object>> getSaved(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        List<String> ids = user.getSavedInternships();
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(Map.of("saved", List.of()));
        }

        List<Internship> internships = internshipRepository.findByIdIn(ids);
        return ResponseEntity.ok(Map.of("saved", internships));
    }

    // GET /api/users/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        long applied     = applicationRepository.findByUserId(user.getId()).size();
        long saved       = user.getSavedInternships() != null ? user.getSavedInternships().size() : 0;
        long reviewing   = applicationRepository.findByUserId(user.getId()).stream()
                            .filter(a -> "reviewing".equals(a.getStatus())).count();
        long shortlisted = applicationRepository.findByUserId(user.getId()).stream()
                            .filter(a -> "shortlisted".equals(a.getStatus()) || "selected".equals(a.getStatus())).count();

        return ResponseEntity.ok(Map.of(
            "applied", applied,
            "saved", saved,
            "reviewing", reviewing,
            "shortlisted", shortlisted
        ));
    }

    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> u = new HashMap<>();
        u.put("_id", user.getId());
        u.put("firstName", user.getFirstName());
        u.put("lastName", user.getLastName());
        u.put("email", user.getEmail());
        u.put("phone", user.getPhone());
        u.put("role", user.getRole());
        u.put("college", user.getCollege());
        u.put("degree", user.getDegree());
        u.put("graduationYear", user.getGraduationYear());
        u.put("skills", user.getSkills());
        u.put("bio", user.getBio());
        u.put("savedInternships", user.getSavedInternships());
        return u;
    }
}
