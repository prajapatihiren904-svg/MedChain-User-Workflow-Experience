package com.medchain.dao;

import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.model.Courier;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Courier entity. Manages registration, logins, and availability tracking.
 */
public class CourierDao {

    public Courier insertCourier(Courier courier) throws DuplicateUserException {
        Connection conn = DBConnection.getConnection();

        // Check for duplicate email
        String checkSql = "SELECT COUNT(*) FROM Couriers WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courier.getEmail());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateUserException("A courier with email " + courier.getEmail() + " already exists!");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error verifying unique courier email", e);
        }

        String sql = "INSERT INTO Couriers (full_name, email, password_hash, phone, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, courier.getFullName());
            pstmt.setString(2, courier.getEmail());
            pstmt.setString(3, courier.getPasswordHash());
            pstmt.setString(4, courier.getPhone());
            pstmt.setString(5, courier.getStatus()); // 'AVAILABLE'

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    courier.setId(gKeys.getInt(1));
                    return courier;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating courier account", e);
        }
        return null;
    }

    public Courier authenticate(String email, String passwordHash) throws InvalidLoginException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT courier_id, full_name, email, phone, status FROM Couriers WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Courier courier = new Courier();
                    courier.setId(rs.getInt("courier_id"));
                    courier.setFullName(rs.getString("full_name"));
                    courier.setEmail(rs.getString("email"));
                    courier.setPhone(rs.getString("phone"));
                    courier.setStatus(rs.getString("status"));
                    return courier;
                } else {
                    throw new InvalidLoginException("Incorrect email or password for Courier login.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during courier authentication", e);
        }
    }

    public Courier getCourierById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT courier_id, full_name, email, phone, status FROM Couriers WHERE courier_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Courier courier = new Courier();
                    courier.setId(rs.getInt("courier_id"));
                    courier.setFullName(rs.getString("full_name"));
                    courier.setEmail(rs.getString("email"));
                    courier.setPhone(rs.getString("phone"));
                    courier.setStatus(rs.getString("status"));
                    return courier;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving courier profile", e);
        }
        return null;
    }

    public Courier getAvailableCourier() {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT courier_id, full_name, email, phone, status FROM Couriers WHERE status = 'AVAILABLE' LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                Courier courier = new Courier();
                courier.setId(rs.getInt("courier_id"));
                courier.setFullName(rs.getString("full_name"));
                courier.setEmail(rs.getString("email"));
                courier.setPhone(rs.getString("phone"));
                courier.setStatus(rs.getString("status"));
                return courier;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error searching for available courier", e);
        }
        return null;
    }

    public boolean updateStatus(int id, String status) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Couriers SET status = ? WHERE courier_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating courier status", e);
        }
    }

    public List<Courier> getAllCouriers() {
        List<Courier> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT courier_id, full_name, email, phone, status FROM Couriers ORDER BY courier_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Courier c = new Courier();
                c.setId(rs.getInt("courier_id"));
                c.setFullName(rs.getString("full_name"));
                c.setEmail(rs.getString("email"));
                c.setPhone(rs.getString("phone"));
                c.setStatus(rs.getString("status"));
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all couriers", e);
        }
        return list;
    }
}
