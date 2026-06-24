package com.naukrisaathi.dto;

import java.util.List;

public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String college;
    private String degree;
    private Integer graduationYear;
    private List<String> skills;
    private String bio;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
