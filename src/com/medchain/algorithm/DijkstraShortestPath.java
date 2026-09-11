package com.medchain.algorithm;

import java.util.*;

/**
 * Package: com.medchain.algorithm
 * Purpose: Performs Dijkstra's shortest path routing over the AreaGraph to find the nearest pharmacy holding inventory.
 */
public class DijkstraShortestPath {

    public static class PathResult {
        private final double distance;
        private final List<Integer> path;

        public PathResult(double distance, List<Integer> path) {
            this.distance = distance;
            this.path = path;
        }

        public double getDistance() {
            return distance;
        }

        public List<Integer> getPath() {
            return path;
        }
    }

    private static class Node implements Comparable<Node> {
        int areaId;
        double distance;

        Node(int areaId, double distance) {
            this.areaId = areaId;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public static PathResult findShortestPath(AreaGraph graph, int sourceAreaId, int targetAreaId) {
        Map<Integer, List<AreaGraph.Edge>> adj = graph.getAdjacencyList();
        
        // If source or target are invalid/not in graph
        if (!adj.containsKey(sourceAreaId) || !adj.containsKey(targetAreaId)) {
            return new PathResult(Double.MAX_VALUE, Collections.emptyList());
        }

        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> parents = new HashMap<>();
        PriorityQueue<Node> pq = new java.util.PriorityQueue<>();

        // Initialize distances
        for (Integer areaId : adj.keySet()) {
            distances.put(areaId, Double.MAX_VALUE);
        }

        distances.put(sourceAreaId, 0.0);
        pq.add(new Node(sourceAreaId, 0.0));

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int u = curr.areaId;

            if (curr.distance > distances.get(u)) {
                continue;
            }

            if (u == targetAreaId) {
                break; // Found shortest path to target
            }

            List<AreaGraph.Edge> neighbors = adj.get(u);
            if (neighbors != null) {
                for (AreaGraph.Edge edge : neighbors) {
                    int v = edge.getTargetAreaId();
                    double weight = edge.getDistanceKm();
                    double newDist = distances.get(u) + weight;

                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        parents.put(v, u);
                        pq.add(new Node(v, newDist));
                    }
                }
            }
        }

        double shortestDistance = distances.get(targetAreaId);
        if (shortestDistance == Double.MAX_VALUE) {
            return new PathResult(Double.MAX_VALUE, Collections.emptyList());
        }

        // Reconstruct path
        List<Integer> path = new LinkedList<>();
        Integer step = targetAreaId;
        while (step != null) {
            path.add(0, step);
            step = parents.get(step);
        }

        return new PathResult(shortestDistance, path);
    }
}
