package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for stock transfers between pharmacies containing source, destination, routing, distance and courier assignment.
 */
public class Transfer {
    private int transferId;
    private int orderId;
    private int sourcePharmacyId;
    private int destinationPharmacyId;
    private int medicineId;
    private int quantity;
    private Integer courierId; // Nullable
    private String transferStatus; // 'PENDING', 'ASSIGNED', 'IN_TRANSIT', 'COMPLETED'
    private double distanceKm;

    // Transient attributes for helper UI printing
    private String sourcePharmacyName;
    private String destinationPharmacyName;
    private String medicineName;
    private String courierName;

    public Transfer() {}

    public Transfer(int transferId, int orderId, int sourcePharmacyId, int destinationPharmacyId, int medicineId, int quantity, Integer courierId, String transferStatus, double distanceKm) {
        this.transferId = transferId;
        this.orderId = orderId;
        this.sourcePharmacyId = sourcePharmacyId;
        this.destinationPharmacyId = destinationPharmacyId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.courierId = courierId;
        this.transferStatus = transferStatus;
        this.distanceKm = distanceKm;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getSourcePharmacyId() {
        return sourcePharmacyId;
    }

    public void setSourcePharmacyId(int sourcePharmacyId) {
        this.sourcePharmacyId = sourcePharmacyId;
    }

    public int getDestinationPharmacyId() {
        return destinationPharmacyId;
    }

    public void setDestinationPharmacyId(int destinationPharmacyId) {
        this.destinationPharmacyId = destinationPharmacyId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getCourierId() {
        return courierId;
    }

    public void setCourierId(Integer courierId) {
        this.courierId = courierId;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getSourcePharmacyName() {
        return sourcePharmacyName;
    }

    public void setSourcePharmacyName(String sourcePharmacyName) {
        this.sourcePharmacyName = sourcePharmacyName;
    }

    public String getDestinationPharmacyName() {
        return destinationPharmacyName;
    }

    public void setDestinationPharmacyName(String destinationPharmacyName) {
        this.destinationPharmacyName = destinationPharmacyName;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "transferId=" + transferId +
                ", orderId=" + orderId +
                ", sourcePharmacyId=" + sourcePharmacyId +
                ", destinationPharmacyId=" + destinationPharmacyId +
                ", medicineId=" + medicineId +
                ", quantity=" + quantity +
                ", courierId=" + courierId +
                ", transferStatus='" + transferStatus + '\'' +
                ", distanceKm=" + distanceKm +
                '}';
    }
}
