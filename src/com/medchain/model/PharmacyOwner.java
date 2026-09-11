package com.medchain.model;

import java.sql.Timestamp;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for Pharmacy Owners extending User to represent pharmacy profiles.
 */
public class PharmacyOwner extends User {
    // [CONCEPT: Inheritance]
    private String pharmacyName;
    private int areaId;
    private String address;
    private String approvalStatus; // 'PENDING', 'APPROVED', 'REJECTED'
    private Timestamp registeredOn;

    public PharmacyOwner() {
        super();
    }

    public PharmacyOwner(int id, String pharmacyName, String ownerName, String email, String passwordHash, int areaId, String address, String approvalStatus, Timestamp registeredOn) {
        super(id, ownerName, email, passwordHash);
        this.pharmacyName = pharmacyName;
        this.areaId = areaId;
        this.address = address;
        this.approvalStatus = approvalStatus;
        this.registeredOn = registeredOn;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Timestamp getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(Timestamp registeredOn) {
        this.registeredOn = registeredOn;
    }

    @Override
    public String toString() {
        return "PharmacyOwner{" +
                "pharmacyId=" + id +
                ", pharmacyName='" + pharmacyName + '\'' +
                ", ownerName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", areaId=" + areaId +
                ", address='" + address + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", registeredOn=" + registeredOn +
                '}';
    }
}
