package com.medchain.model;

import java.time.LocalDate;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for pharmacy specific inventory, storing medicine stocks, pricing, and batch details.
 */
public class Inventory {
    private int inventoryId;
    private int pharmacyId;
    private int medicineId;
    private String batchNumber;
    private LocalDate expiryDate;
    private int stockQuantity;
    private double purchasePrice;
    private double sellingPrice;

    // Transient attributes for UI presentation
    private String medicineName;

    public Inventory() {}

    public Inventory(int inventoryId, int pharmacyId, int medicineId, String batchNumber, LocalDate expiryDate, int stockQuantity, double purchasePrice, double sellingPrice) {
        this.inventoryId = inventoryId;
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.stockQuantity = stockQuantity;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", pharmacyId=" + pharmacyId +
                ", medicineId=" + medicineId +
                ", batchNumber='" + batchNumber + '\'' +
                ", expiryDate=" + expiryDate +
                ", stockQuantity=" + stockQuantity +
                ", purchasePrice=" + purchasePrice +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
