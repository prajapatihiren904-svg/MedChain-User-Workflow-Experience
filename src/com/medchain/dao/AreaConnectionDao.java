package com.medchain.dao;

import com.medchain.model.AreaConnection;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Area Connection entity.
 */
public class    AreaConnectionDao {

    public List<AreaConnection> getAllConnections() {
        List<AreaConnection> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT connection_id, area_id_1, area_id_2, distance_km FROM Area_Connections";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AreaConnection(
                        rs.getInt("connection_id"),
                        rs.getInt("area_id_1"),
                        rs.getInt("area_id_2"),
                        rs.getDouble("distance_km")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching connections", e);
        }
        return list;
    }

    public boolean insertConnection(int area1, int area2, double distance) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Area_Connections (area_id_1, area_id_2, distance_km) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, area1);
            pstmt.setInt(2, area2);
            pstmt.setDouble(3, distance);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting connection: " + area1 + " - " + area2, e);
        }
    }
}
