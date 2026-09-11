package com.medchain.dao;

import com.medchain.model.Area;
import com.medchain.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.dao
 * Purpose: Data Access Object for Area entity.
 */
public class AreaDao {

    public List<Area> getAllAreas() {
        List<Area> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT area_id, area_name FROM Areas ORDER BY area_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Area(rs.getInt("area_id"), rs.getString("area_name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching areas", e);
        }
        return list;
    }

    public Area getAreaById(int id) {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT area_id, area_name FROM Areas WHERE area_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Area(rs.getInt("area_id"), rs.getString("area_name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching area by id: " + id, e);
        }
        return null;
    }

    public Area insertArea(String name) {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO Areas (area_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Area(rs.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting area: " + name, e);
        }
        return null;
    }
}
