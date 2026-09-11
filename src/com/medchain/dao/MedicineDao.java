package com.medchain.dao;

import com.medchain.model.Medicine;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Medicine entity (catalog). Handles lookups and catalog insertion.
 */
public class MedicineDao {

    public Medicine insertMedicine(Medicine med) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Medicines (medicine_name, manufacturer, category) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, med.getMedicineName());
            pstmt.setString(2, med.getManufacturer());
            pstmt.setString(3, med.getCategory());
            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    med.setMedicineId(gKeys.getInt(1));
                    return med;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error inserting medicine catalog item", e);
        }
        return null;
    }

    public Medicine getMedicineById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT medicine_id, medicine_name, manufacturer, category FROM Medicines WHERE medicine_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Medicine(
                            rs.getInt("medicine_id"),
                            rs.getString("medicine_name"),
                            rs.getString("manufacturer"),
                            rs.getString("category")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching medicine by id", e);
        }
        return null;
    }

    public List<Medicine> searchMedicinesByName(String nameQuery) {
        List<Medicine> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT medicine_id, medicine_name, manufacturer, category FROM Medicines WHERE medicine_name LIKE ? ORDER BY medicine_name";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nameQuery + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Medicine(
                            rs.getInt("medicine_id"),
                            rs.getString("medicine_name"),
                            rs.getString("manufacturer"),
                            rs.getString("category")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error searching medicines catalog", e);
        }
        return list;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT medicine_id, medicine_name, manufacturer, category FROM Medicines ORDER BY medicine_name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Medicine(
                        rs.getInt("medicine_id"),
                        rs.getString("medicine_name"),
                        rs.getString("manufacturer"),
                        rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error fetching all medicines", e);
        }
        return list;
    }
}
