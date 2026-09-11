package com.medchain.dao;

import com.medchain.model.Bill;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Bill entity. Handles bill generation and retrievals.
 */
public class BillDao {

    public Bill insertBill(Bill bill) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Bills (order_id, invoice_number, subtotal, gst_amount, total_amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, bill.getOrderId());
            pstmt.setString(2, bill.getInvoiceNumber());
            pstmt.setDouble(3, bill.getSubtotal());
            pstmt.setDouble(4, bill.getGstAmount());
            pstmt.setDouble(5, bill.getTotalAmount());

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    bill.setBillId(gKeys.getInt(1));
                    bill.setBillDate(new Timestamp(System.currentTimeMillis()));
                    return bill;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error generating bill record", e);
        }
        return null;
    }

    public Bill getBillByOrderId(int orderId) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT bill_id, order_id, invoice_number, subtotal, gst_amount, total_amount, bill_date FROM Bills WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Bill(
                            rs.getInt("bill_id"),
                            rs.getInt("order_id"),
                            rs.getString("invoice_number"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("gst_amount"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("bill_date")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching bill details by order ID", e);
        }
        return null;
    }

    public List<Bill> getBillsByCustomer(int customerId) {
        List<Bill> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT b.bill_id, b.order_id, b.invoice_number, b.subtotal, b.gst_amount, b.total_amount, b.bill_date " +
                     "FROM Bills b " +
                     "INNER JOIN Orders o ON b.order_id = o.order_id " +
                     "WHERE o.customer_id = ? ORDER BY b.bill_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Bill(
                            rs.getInt("bill_id"),
                            rs.getInt("order_id"),
                            rs.getString("invoice_number"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("gst_amount"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("bill_date")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching customer bills", e);
        }
        return list;
    }
}
