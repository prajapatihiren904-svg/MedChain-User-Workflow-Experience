package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for individual items within an order.
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int medicineId;
    private int quantity;
    private double unitPrice;

    // Transient attributes for helper UI printing
    private String medicineName;

    public OrderItem() {}

    public OrderItem(int orderItemId, int orderId, int medicineId, int quantity, double unitPrice) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", medicineId=" + medicineId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
