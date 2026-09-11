package com.medchain.algorithm;

import com.medchain.util.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package: com.medchain.algorithm
 * Purpose: In-memory representation of Areas graph built from DB tables Areas and AreaConnections.
 */
public class AreaGraph {
    
    public static class Edge {
        private final int targetAreaId;
        private final double distanceKm;

        public Edge(int targetAreaId, double distanceKm) {
            this.targetAreaId = targetAreaId;
            this.distanceKm = distanceKm;
        }

        public int getTargetAreaId() {
            return targetAreaId;
        }

        public double getDistanceKm() {
            return distanceKm;
        }
    }

    private final Map<Integer, List<Edge>> adjacencyList;
    private final Map<Integer, String> areaNames;

    public AreaGraph() {
        this.adjacencyList = new HashMap<>();
        this.areaNames = new HashMap<>();
        loadGraphFromDb();
    }

    private void loadGraphFromDb() {
        Connection conn = DBConnection.getConnection();
        try {
            // Load all area names
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT area_id, area_name FROM Areas")) {
                while (rs.next()) {
                    int id = rs.getInt("area_id");
                    String name = rs.getString("area_name");
                    areaNames.put(id, name);
                    adjacencyList.put(id, new ArrayList<>());
                }
            }

            // Load all connections (graph is undirected)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT area_id_1, area_id_2, distance_km FROM Area_Connections")) {
                while (rs.next()) {
                    int a1 = rs.getInt("area_id_1");
                    int a2 = rs.getInt("area_id_2");
                    double dist = rs.getDouble("distance_km");

                    // Ensure key exists, and add edge in both directions
                    if (adjacencyList.containsKey(a1) && adjacencyList.containsKey(a2)) {
                        adjacencyList.get(a1).add(new Edge(a2, dist));
                        adjacencyList.get(a2).add(new Edge(a1, dist));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing AreaGraph from Database: " + e.getMessage());
        }
    }

    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public String getAreaName(int areaId) {
        return areaNames.getOrDefault(areaId, "Unknown Area (" + areaId + ")");
    }

    public Map<Integer, String> getAreaNames() {
        return areaNames;
    }
}
