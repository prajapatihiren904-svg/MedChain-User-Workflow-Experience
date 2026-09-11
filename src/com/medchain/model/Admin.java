package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for Admin extending User.
 */
public class Admin extends User {
    // [CONCEPT: Inheritance]
    public Admin() {
        super();
    }

    public Admin(int id, String fullName, String email, String passwordHash) {
        super(id, fullName, email, passwordHash);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
