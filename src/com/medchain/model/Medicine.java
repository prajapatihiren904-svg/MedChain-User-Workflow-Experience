package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for medicine items in the global catalog.
 */
public class Medicine {
    private int medicineId;
    private String medicineName;
    private String manufacturer;
    private String category;

    public Medicine() {}

    public Medicine(int medicineId, String medicineName, String manufacturer, String category) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.manufacturer = manufacturer;
        this.category = category;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {

        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "medicineId=" + medicineId +
                ", medicineName='" + medicineName + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
