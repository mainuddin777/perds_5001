import java.util.*;

/**
 * Implements A* algorithm for optimized pathfinding with heuristics
 * Time Complexity: O((V + E) log V) in practice, better than Dijkstra with good heuristic
 */
public class AStarPathfinder {
    private EmergencyNetworkGraph graph;

    public AStarPathfinder(EmergencyNetworkGraph graph) {
        this.graph = graph;
    }

    public PathResult findShortestPath(Location source, Location destination) {
        Map<Location, Double> gScore = new HashMap<>();
        Map<Location, Double> fScore = new HashMap<>();
        Map<Location, Location> cameFrom = new HashMap<>();
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();

        for (Location loc : graph.getAllLocations()) {
            gScore.put(loc, Double.POSITIVE_INFINITY);
            fScore.put(loc, Double.POSITIVE_INFINITY);
        }

        gScore.put(source, 0.0);
        fScore.put(source, heuristic(source, destination));
        openSet.offer(new AStarNode(source, fScore.get(source)));

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            Location currentLoc = current.location;

            if (currentLoc.equals(destination)) {
                return reconstructPath(source, destination, cameFrom, gScore.get(destination));
            }

            if (current.fScore > fScore.get(currentLoc)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(currentLoc)) {
                Location neighbor = edge.getDestination();
                double tentativeGScore = gScore.get(currentLoc) + edge.getWeight();

                if (tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, currentLoc);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, destination));
                    openSet.offer(new AStarNode(neighbor, fScore.get(neighbor)));
                }
            }
        }

        return null;
    }

    private double heuristic(Location from, Location to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = 6371 * c;

        return (distanceKm / 80.0) * 60.0;
    }

    private PathResult reconstructPath(Location start, Location goal,
                                      Map<Location, Location> cameFrom, double totalCost) {
        List<Location> path = new ArrayList<>();
        Location current = goal;

        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        return new PathResult(path, totalCost);
    }

    private static class AStarNode implements Comparable<AStarNode> {
        Location location;
        double fScore;

        AStarNode(Location location, double fScore) {
            this.location = location;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    public static class PathResult {
        private List<Location> path;
        private double totalTime;

        public PathResult(List<Location> path, double totalTime) {
            this.path = path;
            this.totalTime = totalTime;
        }

        public List<Location> getPath() {
            return path;
        }

        public double getTotalTime() {
            return totalTime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Path (").append(String.format("%.2f", totalTime)).append(" min): ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) sb.append(" -> ");
            }
            return sb.toString();
        }
    }
}