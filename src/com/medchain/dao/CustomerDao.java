package com.medchain.dao;

import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.model.Customer;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Customer entity. Handles registration and authentication.
 */
public class CustomerDao {

    public Customer insertCustomer(Customer cust) throws DuplicateUserException {
        Connection conn = DBConnection.getConnection();
        
        // Check for duplicate email
        String checkSql = "SELECT COUNT(*) FROM Customers WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, cust.getEmail());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateUserException("A customer with email " + cust.getEmail() + " already exists!");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error verifying unique customer email", e);
        }

        String sql = "INSERT INTO Customers (full_name, email, password_hash, phone, home_area_id, preferred_pharmacy_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cust.getFullName());
            pstmt.setString(2, cust.getEmail());
            pstmt.setString(3, cust.getPasswordHash());
            pstmt.setString(4, cust.getPhone());
            pstmt.setInt(5, cust.getHomeAreaId());
            if (cust.getPreferredPharmacyId() != null) {
                pstmt.setInt(6, cust.getPreferredPharmacyId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    cust.setId(gKeys.getInt(1));
                    cust.setRegisteredOn(new Timestamp(System.currentTimeMillis()));
                    return cust;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating customer account", e);
        }
        return null;
    }

    public Customer authenticate(String email, String passwordHash) throws InvalidLoginException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT customer_id, full_name, email, phone, home_area_id, preferred_pharmacy_id, registered_on FROM Customers WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer cust = new Customer();
                    cust.setId(rs.getInt("customer_id"));
                    cust.setFullName(rs.getString("full_name"));
                    cust.setEmail(rs.getString("email"));
                    cust.setPhone(rs.getString("phone"));
                    cust.setHomeAreaId(rs.getInt("home_area_id"));
                    int prefId = rs.getInt("preferred_pharmacy_id");
                    cust.setPreferredPharmacyId(rs.wasNull() ? null : prefId);
                    cust.setRegisteredOn(rs.getTimestamp("registered_on"));
                    return cust;
                } else {
                    throw new InvalidLoginException("Incorrect email or password for Customer login.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during customer authentication", e);
        }
    }

    public Customer getCustomerById(int customerId) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT customer_id, full_name, email, phone, home_area_id, preferred_pharmacy_id, registered_on FROM Customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer cust = new Customer();
                    cust.setId(rs.getInt("customer_id"));
                    cust.setFullName(rs.getString("full_name"));
                    cust.setEmail(rs.getString("email"));
                    cust.setPhone(rs.getString("phone"));
                    cust.setHomeAreaId(rs.getInt("home_area_id"));
                    int prefId = rs.getInt("preferred_pharmacy_id");
                    cust.setPreferredPharmacyId(rs.wasNull() ? null : prefId);
                    cust.setRegisteredOn(rs.getTimestamp("registered_on"));
                    return cust;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching customer profile", e);
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT customer_id, full_name, email, phone, home_area_id, preferred_pharmacy_id, registered_on FROM Customers ORDER BY customer_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer cust = new Customer();
                cust.setId(rs.getInt("customer_id"));
                cust.setFullName(rs.getString("full_name"));
                cust.setEmail(rs.getString("email"));
                cust.setPhone(rs.getString("phone"));
                cust.setHomeAreaId(rs.getInt("home_area_id"));
                int prefId = rs.getInt("preferred_pharmacy_id");
                cust.setPreferredPharmacyId(rs.wasNull() ? null : prefId);
                cust.setRegisteredOn(rs.getTimestamp("registered_on"));
                list.add(cust);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all customers", e);
        }
        return list;
    }
}
