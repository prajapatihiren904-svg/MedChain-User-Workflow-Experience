package com.medchain.model;

import java.sql.Timestamp;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for Customers extending User to represent customer-specific profile and preferences.
 */
public class Customer extends User {
    // [CONCEPT: Inheritance]
    private String phone;
    private int homeAreaId;
    private Integer preferredPharmacyId; // Nullable
    private Timestamp registeredOn;

    public Customer() {
        super();
    }

    public Customer(int id, String fullName, String email, String passwordHash, String phone, int homeAreaId, Integer preferredPharmacyId, Timestamp registeredOn) {
        super(id, fullName, email, passwordHash);
        this.phone = phone;
        this.homeAreaId = homeAreaId;
        this.preferredPharmacyId = preferredPharmacyId;
        this.registeredOn = registeredOn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getHomeAreaId() {
        return homeAreaId;
    }

    public void setHomeAreaId(int homeAreaId) {
        this.homeAreaId = homeAreaId;
    }

    public Integer getPreferredPharmacyId() {
        return preferredPharmacyId;
    }

    public void setPreferredPharmacyId(Integer preferredPharmacyId) {
        this.preferredPharmacyId = preferredPharmacyId;
    }

    public Timestamp getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(Timestamp registeredOn) {
        this.registeredOn = registeredOn;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", homeAreaId=" + homeAreaId +
                ", preferredPharmacyId=" + preferredPharmacyId +
                ", registeredOn=" + registeredOn +
                '}';
    }
}
