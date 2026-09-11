package com.medchain.dao;

import com.medchain.model.OrderItem;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Order Item entity.
 */
public class OrderItemDao {

    public boolean insertOrderItem(OrderItem item) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Order_Items (order_id, medicine_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getOrderId());
            pstmt.setInt(2, item.getMedicineId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getUnitPrice());

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        item.setOrderItemId(gKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error adding order item", e);
        }
        return false;
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.medicine_id, oi.quantity, oi.unit_price, m.medicine_name " +
                     "FROM Order_Items oi " +
                     "INNER JOIN Medicines m ON oi.medicine_id = m.medicine_id " +
                     "WHERE oi.order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    );
                    item.setMedicineName(rs.getString("medicine_name"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving items for order ID: " + orderId, e);
        }
        return list;
    }
}
