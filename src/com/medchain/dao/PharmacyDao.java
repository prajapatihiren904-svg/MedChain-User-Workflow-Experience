package com.medchain.dao;

import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.exception.PharmacyNotApprovedException;
import com.medchain.model.PharmacyOwner;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Pharmacies entity. Validates registration, approval status and logins.
 */
public class PharmacyDao {

    public PharmacyOwner insertPharmacy(PharmacyOwner pharm) throws DuplicateUserException {
        Connection conn = DBConnection.getConnection();

        // Check for duplicate email
        String checkSql = "SELECT COUNT(*) FROM Pharmacies WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, pharm.getEmail());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateUserException("A pharmacy owner with email " + pharm.getEmail() + " already exists!");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error verifying unique pharmacy email", e);
        }

        String sql = "INSERT INTO Pharmacies (pharmacy_name, owner_name, email, password_hash, area_id, address, approval_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, pharm.getPharmacyName());
            pstmt.setString(2, pharm.getFullName());
            pstmt.setString(3, pharm.getEmail());
            pstmt.setString(4, pharm.getPasswordHash());
            pstmt.setInt(5, pharm.getAreaId());
            pstmt.setString(6, pharm.getAddress());
            pstmt.setString(7, pharm.getApprovalStatus()); // Default 'PENDING'

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    pharm.setId(gKeys.getInt(1));
                    pharm.setRegisteredOn(new Timestamp(System.currentTimeMillis()));
                    return pharm;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error registering pharmacy profile", e);
        }
        return null;
    }

    public PharmacyOwner authenticate(String email, String passwordHash) 
            throws InvalidLoginException, PharmacyNotApprovedException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT pharmacy_id, pharmacy_name, owner_name, email, area_id, address, approval_status, registered_on FROM Pharmacies WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("approval_status");
                    if (!"APPROVED".equals(status)) {
                        throw new PharmacyNotApprovedException("Your pharmacy account is " + status + ". Please wait for Admin approval.");
                    }

                    PharmacyOwner owner = new PharmacyOwner();
                    owner.setId(rs.getInt("pharmacy_id"));
                    owner.setPharmacyName(rs.getString("pharmacy_name"));
                    owner.setFullName(rs.getString("owner_name"));
                    owner.setEmail(rs.getString("email"));
                    owner.setAreaId(rs.getInt("area_id"));
                    owner.setAddress(rs.getString("address"));
                    owner.setApprovalStatus(status);
                    owner.setRegisteredOn(rs.getTimestamp("registered_on"));
                    return owner;
                } else {
                    throw new InvalidLoginException("Incorrect email or password for Pharmacy login.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during pharmacy owner authentication", e);
        }
    }

    public PharmacyOwner getPharmacyById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT pharmacy_id, pharmacy_name, owner_name, email, area_id, address, approval_status, registered_on FROM Pharmacies WHERE pharmacy_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PharmacyOwner owner = new PharmacyOwner();
                    owner.setId(rs.getInt("pharmacy_id"));
                    owner.setPharmacyName(rs.getString("pharmacy_name"));
                    owner.setFullName(rs.getString("owner_name"));
                    owner.setEmail(rs.getString("email"));
                    owner.setAreaId(rs.getInt("area_id"));
                    owner.setAddress(rs.getString("address"));
                    owner.setApprovalStatus(rs.getString("approval_status"));
                    owner.setRegisteredOn(rs.getTimestamp("registered_on"));
                    return owner;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving pharmacy: " + id, e);
        }
        return null;
    }

    public boolean updateApprovalStatus(int id, String status) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Pharmacies SET approval_status = ? WHERE pharmacy_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating pharmacy approval status", e);
        }
    }

    public List<PharmacyOwner> getAllPharmacies() {
        List<PharmacyOwner> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT pharmacy_id, pharmacy_name, owner_name, email, area_id, address, approval_status, registered_on FROM Pharmacies ORDER BY pharmacy_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PharmacyOwner owner = new PharmacyOwner();
                owner.setId(rs.getInt("pharmacy_id"));
                owner.setPharmacyName(rs.getString("pharmacy_name"));
                owner.setFullName(rs.getString("owner_name"));
                owner.setEmail(rs.getString("email"));
                owner.setAreaId(rs.getInt("area_id"));
                owner.setAddress(rs.getString("address"));
                owner.setApprovalStatus(rs.getString("approval_status"));
                owner.setRegisteredOn(rs.getTimestamp("registered_on"));
                list.add(owner);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all pharmacies", e);
        }
        return list;
    }
}
