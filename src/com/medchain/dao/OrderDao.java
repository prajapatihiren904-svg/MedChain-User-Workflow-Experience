package com.medchain.dao;

import com.medchain.model.Order;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Order entity. Manages orders and holds the inner-join detailed reporting query.
 */
public class OrderDao {

    public Order insertOrder(Order order) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Orders (customer_id, pharmacy_id, order_status) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, order.getCustomerId());
            pstmt.setInt(2, order.getPharmacyId());
            pstmt.setString(3, order.getOrderStatus()); // 'PLACED'

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    order.setOrderId(gKeys.getInt(1));
                    order.setOrderDate(new Timestamp(System.currentTimeMillis()));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error placing order", e);
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating order status", e);
        }
    }

    public Order getOrderById(int orderId) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT o.order_id, o.customer_id, o.pharmacy_id, o.order_status, o.order_date, c.full_name AS customer_name, p.pharmacy_name " +
                     "FROM Orders o " +
                     "INNER JOIN Customers c ON o.customer_id = c.customer_id " +
                     "INNER JOIN Pharmacies p ON o.pharmacy_id = p.pharmacy_id " +
                     "WHERE o.order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getString("order_status"),
                            rs.getTimestamp("order_date")
                    );
                    order.setCustomerName(rs.getString("customer_name"));
                    order.setPharmacyName(rs.getString("pharmacy_name"));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving order profile", e);
        }
        return null;
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT o.order_id, o.customer_id, o.pharmacy_id, o.order_status, o.order_date, c.full_name AS customer_name, p.pharmacy_name " +
                     "FROM Orders o " +
                     "INNER JOIN Customers c ON o.customer_id = c.customer_id " +
                     "INNER JOIN Pharmacies p ON o.pharmacy_id = p.pharmacy_id " +
                     "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getString("order_status"),
                            rs.getTimestamp("order_date")
                    );
                    order.setCustomerName(rs.getString("customer_name"));
                    order.setPharmacyName(rs.getString("pharmacy_name"));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching customer orders history", e);
        }
        return list;
    }

    public List<Order> getOrdersByPharmacy(int pharmacyId) {
        List<Order> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT o.order_id, o.customer_id, o.pharmacy_id, o.order_status, o.order_date, c.full_name AS customer_name, p.pharmacy_name " +
                     "FROM Orders o " +
                     "INNER JOIN Customers c ON o.customer_id = c.customer_id " +
                     "INNER JOIN Pharmacies p ON o.pharmacy_id = p.pharmacy_id " +
                     "WHERE o.pharmacy_id = ? ORDER BY o.order_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pharmacyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getString("order_status"),
                            rs.getTimestamp("order_date")
                    );
                    order.setCustomerName(rs.getString("customer_name"));
                    order.setPharmacyName(rs.getString("pharmacy_name"));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching pharmacy orders", e);
        }
        return list;
    }

    // [CONCEPT: JDBC - Prepared Statement inner join mapping demonstration]
    public List<Order> getOrdersWithDetails() {
        List<Order> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT o.order_id, o.customer_id, o.pharmacy_id, o.order_status, o.order_date, c.full_name AS customer_name, p.pharmacy_name " +
                     "FROM Orders o " +
                     "INNER JOIN Customers c ON o.customer_id = c.customer_id " +
                     "INNER JOIN Pharmacies p ON o.pharmacy_id = p.pharmacy_id " +
                     "ORDER BY o.order_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("pharmacy_id"),
                        rs.getString("order_status"),
                        rs.getTimestamp("order_date")
                );
                order.setCustomerName(rs.getString("customer_name"));
                order.setPharmacyName(rs.getString("pharmacy_name"));
                list.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error executing detailed inner join orders query", e);
        }
        return list;
    }
}
