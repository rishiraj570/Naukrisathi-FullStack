package com.naukrisaathi.dto;

import com.naukrisaathi.model.User;

public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String college;
    private String role;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.firstName = u.getFirstName();
        r.lastName = u.getLastName();
        r.email = u.getEmail();
        r.phone = u.getPhone();
        r.college = u.getCollege();
        r.role = u.getRole();
        return r;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCollege() { return college; }
    public String getRole() { return role; }
}
