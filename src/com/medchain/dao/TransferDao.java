package com.medchain.dao;

import com.medchain.model.Transfer;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Transfer entity. Manages courier routing, deliveries, and status updates.
 */
public class TransferDao {

    public Transfer insertTransfer(Transfer tr) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Transfers (order_id, source_pharmacy_id, destination_pharmacy_id, medicine_id, quantity, courier_id, transfer_status, distance_km) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tr.getOrderId());
            pstmt.setInt(2, tr.getSourcePharmacyId());
            pstmt.setInt(3, tr.getDestinationPharmacyId());
            pstmt.setInt(4, tr.getMedicineId());
            pstmt.setInt(5, tr.getQuantity());
            if (tr.getCourierId() != null) {
                pstmt.setInt(6, tr.getCourierId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, tr.getTransferStatus()); // 'PENDING'
            pstmt.setDouble(8, tr.getDistanceKm());

            pstmt.executeUpdate();
            try (ResultSet gKeys = pstmt.getGeneratedKeys()) {
                if (gKeys.next()) {
                    tr.setTransferId(gKeys.getInt(1));
                    return tr;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating inventory transfer request", e);
        }
        return null;
    }

    public boolean updateTransferStatus(int transferId, String status) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Transfers SET transfer_status = ? WHERE transfer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, transferId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating transfer status", e);
        }
    }

    public boolean updateTransferCourier(int transferId, int courierId) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE Transfers SET courier_id = ?, transfer_status = 'ASSIGNED' WHERE transfer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courierId);
            pstmt.setInt(2, transferId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error assigning courier to transfer", e);
        }
    }

    public Transfer getTransferById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT t.transfer_id, t.order_id, t.source_pharmacy_id, t.destination_pharmacy_id, t.medicine_id, t.quantity, t.courier_id, t.transfer_status, t.distance_km, " +
                     "p1.pharmacy_name AS source_name, p2.pharmacy_name AS dest_name, m.medicine_name, c.full_name AS courier_name " +
                     "FROM Transfers t " +
                     "INNER JOIN Pharmacies p1 ON t.source_pharmacy_id = p1.pharmacy_id " +
                     "INNER JOIN Pharmacies p2 ON t.destination_pharmacy_id = p2.pharmacy_id " +
                     "INNER JOIN Medicines m ON t.medicine_id = m.medicine_id " +
                     "LEFT JOIN Couriers c ON t.courier_id = c.courier_id " +
                     "WHERE t.transfer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transfer tr = new Transfer(
                            rs.getInt("transfer_id"),
                            rs.getInt("order_id"),
                            rs.getInt("source_pharmacy_id"),
                            rs.getInt("destination_pharmacy_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity"),
                            rs.getInt("courier_id") == 0 && rs.wasNull() ? null : rs.getInt("courier_id"),
                            rs.getString("transfer_status"),
                            rs.getDouble("distance_km")
                    );
                    tr.setSourcePharmacyName(rs.getString("source_name"));
                    tr.setDestinationPharmacyName(rs.getString("dest_name"));
                    tr.setMedicineName(rs.getString("medicine_name"));
                    tr.setCourierName(rs.getString("courier_name"));
                    return tr;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving transfer details: " + id, e);
        }
        return null;
    }

    public List<Transfer> getTransfersByCourier(int courierId) {
        List<Transfer> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT t.transfer_id, t.order_id, t.source_pharmacy_id, t.destination_pharmacy_id, t.medicine_id, t.quantity, t.courier_id, t.transfer_status, t.distance_km, " +
                     "p1.pharmacy_name AS source_name, p2.pharmacy_name AS dest_name, m.medicine_name, c.full_name AS courier_name " +
                     "FROM Transfers t " +
                     "INNER JOIN Pharmacies p1 ON t.source_pharmacy_id = p1.pharmacy_id " +
                     "INNER JOIN Pharmacies p2 ON t.destination_pharmacy_id = p2.pharmacy_id " +
                     "INNER JOIN Medicines m ON t.medicine_id = m.medicine_id " +
                     "INNER JOIN Couriers c ON t.courier_id = c.courier_id " +
                     "WHERE t.courier_id = ? AND t.transfer_status IN ('ASSIGNED', 'IN_TRANSIT') " +
                     "ORDER BY t.transfer_id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transfer tr = new Transfer(
                            rs.getInt("transfer_id"),
                            rs.getInt("order_id"),
                            rs.getInt("source_pharmacy_id"),
                            rs.getInt("destination_pharmacy_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity"),
                            rs.getInt("courier_id"),
                            rs.getString("transfer_status"),
                            rs.getDouble("distance_km")
                    );
                    tr.setSourcePharmacyName(rs.getString("source_name"));
                    tr.setDestinationPharmacyName(rs.getString("dest_name"));
                    tr.setMedicineName(rs.getString("medicine_name"));
                    tr.setCourierName(rs.getString("courier_name"));
                    list.add(tr);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving transfer assigned tasks", e);
        }
        return list;
    }

    public List<Transfer> getAllTransfers() {
        List<Transfer> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT t.transfer_id, t.order_id, t.source_pharmacy_id, t.destination_pharmacy_id, t.medicine_id, t.quantity, t.courier_id, t.transfer_status, t.distance_km, " +
                     "p1.pharmacy_name AS source_name, p2.pharmacy_name AS dest_name, m.medicine_name, c.full_name AS courier_name " +
                     "FROM Transfers t " +
                     "INNER JOIN Pharmacies p1 ON t.source_pharmacy_id = p1.pharmacy_id " +
                     "INNER JOIN Pharmacies p2 ON t.destination_pharmacy_id = p2.pharmacy_id " +
                     "INNER JOIN Medicines m ON t.medicine_id = m.medicine_id " +
                     "LEFT JOIN Couriers c ON t.courier_id = c.courier_id " +
                     "ORDER BY t.transfer_id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transfer tr = new Transfer(
                        rs.getInt("transfer_id"),
                        rs.getInt("order_id"),
                        rs.getInt("source_pharmacy_id"),
                        rs.getInt("destination_pharmacy_id"),
                        rs.getInt("medicine_id"),
                        rs.getInt("quantity"),
                        rs.getInt("courier_id") == 0 && rs.wasNull() ? null : rs.getInt("courier_id"),
                        rs.getString("transfer_status"),
                        rs.getDouble("distance_km")
                );
                tr.setSourcePharmacyName(rs.getString("source_name"));
                tr.setDestinationPharmacyName(rs.getString("dest_name"));
                tr.setMedicineName(rs.getString("medicine_name"));
                tr.setCourierName(rs.getString("courier_name"));
                list.add(tr);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error retrieving all transfer reports", e);
        }
        return list;
    }
}
