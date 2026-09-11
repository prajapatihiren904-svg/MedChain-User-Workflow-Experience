package com.medchain.dao;

import com.medchain.model.Inventory;
import com.medchain.util.DBConnection;
import com.medchain.util.DateUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Inventory entity. Manages stock, batch tracking, prices, and complex cross-joins.
 */
public class InventoryDao {

    public Inventory insertInventory(Inventory inv) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Inventory (pharmacy_id, medicine_id, batch_number, expiry_date, stock_quantity, purchase_price, selling_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, inv.getPharmacyId());
            pstmt.setInt(2, inv.getMedicineId());
            pstmt.setString(3, inv.getBatchNumber());
            pstmt.setDate(4, DateUtil.toSqlDate(inv.getExpiryDate()));
            pstmt.setInt(5, inv.getStockQuantity());
            pstmt.setDouble(6, inv.getPurchasePrice());
            pstmt.setDouble(7, inv.getSellingPrice());

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    inv.setInventoryId(gKeys.getInt(1));
                    return inv;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error inserting inventory batch", e);
        }
        return null;
    }

    public boolean updateStockQuantity(int inventoryId, int newQuantity) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Inventory SET stock_quantity = ? WHERE inventory_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, inventoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating inventory stock", e);
        }
    }

    public boolean deleteInventoryBatch(int pharmacyId, int medicineId, String batchNumber) {
        Connection conn = DBConnection.getConnection();
        String sql = "DELETE FROM Inventory WHERE pharmacy_id = ? AND medicine_id = ? AND batch_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pharmacyId);
            pstmt.setInt(2, medicineId);
            pstmt.setString(3, batchNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting inventory batch", e);
        }
    }

    public List<Inventory> getInventoryByPharmacy(int pharmacyId) {
        List<Inventory> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT i.inventory_id, i.pharmacy_id, i.medicine_id, i.batch_number, i.expiry_date, i.stock_quantity, i.purchase_price, i.selling_price, m.medicine_name " +
                     "FROM Inventory i " +
                     "INNER JOIN Medicines m ON i.medicine_id = m.medicine_id " +
                     "WHERE i.pharmacy_id = ? ORDER BY i.expiry_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pharmacyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Inventory inv = new Inventory(
                            rs.getInt("inventory_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getInt("medicine_id"),
                            rs.getString("batch_number"),
                            DateUtil.toLocalDate(rs.getDate("expiry_date")),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("purchase_price"),
                            rs.getDouble("selling_price")
                    );
                    inv.setMedicineName(rs.getString("medicine_name"));
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving inventory for pharmacy", e);
        }
        return list;
    }

    public List<Inventory> getInventoryByPharmacyAndMedicine(int pharmacyId, int medicineId) {
        List<Inventory> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT i.inventory_id, i.pharmacy_id, i.medicine_id, i.batch_number, i.expiry_date, i.stock_quantity, i.purchase_price, i.selling_price, m.medicine_name " +
                     "FROM Inventory i " +
                     "INNER JOIN Medicines m ON i.medicine_id = m.medicine_id " +
                     "WHERE i.pharmacy_id = ? AND i.medicine_id = ? AND i.stock_quantity > 0 " +
                     "ORDER BY i.expiry_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pharmacyId);
            pstmt.setInt(2, medicineId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Inventory inv = new Inventory(
                            rs.getInt("inventory_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getInt("medicine_id"),
                            rs.getString("batch_number"),
                            DateUtil.toLocalDate(rs.getDate("expiry_date")),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("purchase_price"),
                            rs.getDouble("selling_price")
                    );
                    inv.setMedicineName(rs.getString("medicine_name"));
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving batches", e);
        }
        return list;
    }

    public List<Inventory> searchMedicineStockAcrossApprovedPharmacies(int medicineId) {
        List<Inventory> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT i.inventory_id, i.pharmacy_id, i.medicine_id, i.batch_number, i.expiry_date, i.stock_quantity, i.purchase_price, i.selling_price, m.medicine_name " +
                     "FROM Inventory i " +
                     "INNER JOIN Pharmacies p ON i.pharmacy_id = p.pharmacy_id " +
                     "INNER JOIN Medicines m ON i.medicine_id = m.medicine_id " +
                     "WHERE i.medicine_id = ? AND i.stock_quantity > 0 AND p.approval_status = 'APPROVED' " +
                     "ORDER BY i.selling_price ASC, i.expiry_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, medicineId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Inventory inv = new Inventory(
                            rs.getInt("inventory_id"),
                            rs.getInt("pharmacy_id"),
                            rs.getInt("medicine_id"),
                            rs.getString("batch_number"),
                            DateUtil.toLocalDate(rs.getDate("expiry_date")),
                            rs.getInt("stock_quantity"),
                            rs.getDouble("purchase_price"),
                            rs.getDouble("selling_price")
                    );
                    inv.setMedicineName(rs.getString("medicine_name"));
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error searching stock across approved pharmacies", e);
        }
        return list;
    }

    // [CONCEPT: JDBC - Prepared Statement left join mapping demonstration]
    public List<Map<String, Object>> getPharmaciesWithZeroStock(int medicineId) {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.pharmacy_name, p.owner_name, COALESCE(i.stock_quantity, 0) AS stock " +
                     "FROM Pharmacies p " +
                     "LEFT JOIN Inventory i ON p.pharmacy_id = i.pharmacy_id AND i.medicine_id = ? " +
                     "WHERE (i.stock_quantity IS NULL OR i.stock_quantity = 0) AND p.approval_status = 'APPROVED'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, medicineId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pharmacy_name", rs.getString("pharmacy_name"));
                    map.put("owner_name", rs.getString("owner_name"));
                    map.put("stock", rs.getInt("stock"));
                    results.add(map);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in zero-stock left join query", e);
        }
        return results;
    }
}
