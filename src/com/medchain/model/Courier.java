package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for Courier extending User to represent couriers and their availability status.
 */
public class Courier extends User {
    // [CONCEPT: Inheritance]
    private String phone;
    private String status; // 'AVAILABLE', 'ON_DELIVERY'

    public Courier() {
        super();
    }

    public Courier(int id, String fullName, String email, String passwordHash, String phone, String status) {
        super(id, fullName, email, passwordHash);
        this.phone = phone;
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Courier{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
