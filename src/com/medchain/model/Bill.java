package com.medchain.model;

import java.sql.Timestamp;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for billing invoices, containing totals, GST breakdown and links to orders.
 */
public class Bill {
    private int billId;
    private int orderId;
    private String invoiceNumber;
    private double subtotal;
    private double gstAmount;
    private double totalAmount;
    private Timestamp billDate;

    public Bill() {}

    public Bill(int billId, int orderId, String invoiceNumber, double subtotal, double gstAmount, double totalAmount, Timestamp billDate) {
        this.billId = billId;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.subtotal = subtotal;
        this.gstAmount = gstAmount;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getBillDate() {
        return billDate;
    }

    public void setBillDate(Timestamp billDate) {
        this.billDate = billDate;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", orderId=" + orderId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", subtotal=" + subtotal +
                ", gstAmount=" + gstAmount +
                ", totalAmount=" + totalAmount +
                ", billDate=" + billDate +
                '}';
    }
}
