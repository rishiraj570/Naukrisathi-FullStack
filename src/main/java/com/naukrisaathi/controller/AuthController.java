package com.naukrisaathi.controller;

import com.naukrisaathi.dto.LoginRequest;
import com.naukrisaathi.dto.RegisterRequest;
import com.naukrisaathi.model.User;
import com.naukrisaathi.repository.UserRepository;
import com.naukrisaathi.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? req.getRole() : "student");
        user.setCollege(req.getCollege());

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", sanitizeUser(saved));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail().toLowerCase().trim());

        if (userOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", sanitizeUser(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of("user", sanitizeUser(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // JWT is stateless; client clears the token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
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
