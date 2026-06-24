package com.naukrisaathi.config;

import com.naukrisaathi.model.Internship;
import com.naukrisaathi.repository.InternshipRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InternshipRepository internshipRepository;

    public DataSeeder(InternshipRepository internshipRepository) {
        this.internshipRepository = internshipRepository;
    }

    @Override
    public void run(String... args) {
        if (internshipRepository.count() > 0) return; // already seeded

        List<Internship> internships = List.of(
            build("Software Development Intern", "Google", "#4285F4", "Bangalore (Remote)", "online", 25000, "3 months",
                  List.of("Java", "Spring Boot", "MongoDB"), "Work on Google's core infrastructure."),
            build("Data Science Intern", "Microsoft", "#0078D4", "Hyderabad", "hybrid", 20000, "6 months",
                  List.of("Python", "ML", "TensorFlow"), "Build ML models for Microsoft Azure."),
            build("UI/UX Design Intern", "Amazon", "#FF9900", "Delhi (Remote)", "online", 18000, "3 months",
                  List.of("Figma", "Adobe XD", "CSS"), "Design next-gen e-commerce experiences."),
            build("Backend Engineering Intern", "Flipkart", "#F74F00", "Bangalore", "offline", 22000, "4 months",
                  List.of("Node.js", "MySQL", "Redis"), "Scale Flipkart's backend services."),
            build("Product Management Intern", "Swiggy", "#FC8019", "Bangalore (Remote)", "online", 15000, "3 months",
                  List.of("Analytics", "SQL", "Product Thinking"), "Drive product initiatives for Swiggy."),
            build("Machine Learning Intern", "IBM", "#0043CE", "Mumbai", "hybrid", 20000, "6 months",
                  List.of("Python", "PyTorch", "NLP"), "Develop AI-powered enterprise solutions."),
            build("Full Stack Intern", "Infosys", "#007CC3", "Pune", "offline", 12000, "3 months",
                  List.of("React", "Node.js", "PostgreSQL"), "Build full-stack enterprise apps."),
            build("Cloud DevOps Intern", "Wipro", "#341C6A", "Chennai", "hybrid", 14000, "6 months",
                  List.of("AWS", "Docker", "Kubernetes"), "Manage cloud deployments and CI/CD pipelines."),
            build("Mobile App Intern", "Zepto", "#9B51E0", "Mumbai (Remote)", "online", 16000, "3 months",
                  List.of("Flutter", "Dart", "Firebase"), "Build features for Zepto's mobile app."),
            build("Cybersecurity Intern", "Cisco", "#1BA0D7", "Bangalore", "offline", 18000, "4 months",
                  List.of("Network Security", "Python", "Wireshark"), "Secure Cisco's enterprise networks.")
        );

        internships.forEach(i -> i.setFeatured(true));
        internshipRepository.saveAll(internships);
        System.out.println("✅ Seeded " + internships.size() + " internships into MongoDB");
    }

    private Internship build(String title, String company, String color, String location,
                              String type, int stipend, String duration, List<String> skills, String desc) {
        Internship i = new Internship();
        i.setTitle(title);
        i.setCompany(company);
        i.setCompanyColor(color);
        i.setLocation(location);
        i.setType(type);
        i.setStipend(stipend);
        i.setDuration(duration);
        i.setSkills(skills);
        i.setDescription(desc);
        i.setFeatured(true);
        i.setActive(true);
        i.setApplicantCount((int)(Math.random() * 150) + 10);
        return i;
    }
}
