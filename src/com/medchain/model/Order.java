package com.medchain.model;

import java.sql.Timestamp;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for user orders.
 */
public class Order {
    private int orderId;
    private int customerId;
    private int pharmacyId;
    private String orderStatus; // 'PLACED', 'FULFILLED_LOCAL', 'FULFILLED_TRANSFER', 'CANCELLED'
    private Timestamp orderDate;

    // Transient attributes for helper UI printing
    private String customerName;
    private String pharmacyName;

    public Order() {}

    public Order(int orderId, int customerId, int pharmacyId, String orderStatus, Timestamp orderDate) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.pharmacyId = pharmacyId;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", pharmacyId=" + pharmacyId +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}
