package com.medchain.model;

/**
 * Package: com.medchain.model
 * Purpose: Domain model for area connections (graph edges) with road distance.
 */
public class AreaConnection {
    private int connectionId;
    private int areaId1;
    private int areaId2;
    private double distanceKm;

    public AreaConnection() {}

    public AreaConnection(int connectionId, int areaId1, int areaId2, double distanceKm) {
        this.connectionId = connectionId;
        this.areaId1 = areaId1;
        this.areaId2 = areaId2;
        this.distanceKm = distanceKm;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getAreaId1() {
        return areaId1;
    }

    public void setAreaId1(int areaId1) {
        this.areaId1 = areaId1;
    }

    public int getAreaId2() {
        return areaId2;
    }

    public void setAreaId2(int areaId2) {
        this.areaId2 = areaId2;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    @Override
    public String toString() {
        return "AreaConnection{" +
                "connectionId=" + connectionId +
                ", areaId1=" + areaId1 +
                ", areaId2=" + areaId2 +
                ", distanceKm=" + distanceKm +
                '}';
    }
}
