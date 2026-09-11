package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Base domain model for system users representing common authentication and personal attributes.
 */
public abstract class User {
    // [CONCEPT: Classes & Objects]
    protected int id;
    protected String fullName;
    protected String email;
    protected String passwordHash;

    // [CONCEPT: Constructors]
    public User() {
    }

    public User(int id, String fullName, String email, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // [CONCEPT: Encapsulation]
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // [CONCEPT: Polymorphism - Method Overriding (toString)]
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
