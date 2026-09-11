package com.medchain.dao;

import com.medchain.exception.InvalidLoginException;
import com.medchain.model.Admin;
import com.medchain.util.DBConnection;
import java.sql.*;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Admin entity. Handles administrator login.
 */
public class AdminDao {

    public Admin authenticate(String email, String passwordHash) throws InvalidLoginException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT admin_id, full_name, email FROM Admins WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("admin_id"));
                    admin.setFullName(rs.getString("full_name"));
                    admin.setEmail(rs.getString("email"));
                    return admin;
                } else {
                    throw new InvalidLoginException("Incorrect email or password for Admin login.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during admin authentication", e);
        }
    }

    public Admin getAdminById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT admin_id, full_name, email FROM Admins WHERE admin_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("admin_id"));
                    admin.setFullName(rs.getString("full_name"));
                    admin.setEmail(rs.getString("email"));
                    return admin;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching admin profile: " + id, e);
        }
        return null;
    }
}
