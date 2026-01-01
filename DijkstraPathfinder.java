import java.util.*;

/**
 * Implements Dijkstra's algorithm for finding shortest paths
 * Time Complexity: O((V + E) log V) using priority queue
 */
public class DijkstraPathfinder {
    private EmergencyNetworkGraph graph;

    public DijkstraPathfinder(EmergencyNetworkGraph graph) {
        this.graph = graph;
    }

    public PathResult findShortestPath(Location source, Location destination) {
        Map<Location, Double> distances = new HashMap<>();
        Map<Location, Location> previous = new HashMap<>();
        PriorityQueue<LocationDistance> pq = new PriorityQueue<>();

        for (Location loc : graph.getAllLocations()) {
            distances.put(loc, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);
        pq.offer(new LocationDistance(source, 0.0));

        while (!pq.isEmpty()) {
            LocationDistance current = pq.poll();
            Location currentLoc = current.location;

            if (currentLoc.equals(destination)) {
                return reconstructPath(source, destination, previous, distances.get(destination));
            }

            if (current.distance > distances.get(currentLoc)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(currentLoc)) {
                Location neighbor = edge.getDestination();
                double newDist = distances.get(currentLoc) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, currentLoc);
                    pq.offer(new LocationDistance(neighbor, newDist));
                }
            }
        }

        return null;
    }

    public Map<Location, Double> findDistancesFromSource(Location source) {
        Map<Location, Double> distances = new HashMap<>();
        PriorityQueue<LocationDistance> pq = new PriorityQueue<>();

        for (Location loc : graph.getAllLocations()) {
            distances.put(loc, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);
        pq.offer(new LocationDistance(source, 0.0));

        while (!pq.isEmpty()) {
            LocationDistance current = pq.poll();
            Location currentLoc = current.location;

            if (current.distance > distances.get(currentLoc)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(currentLoc)) {
                Location neighbor = edge.getDestination();
                double newDist = distances.get(currentLoc) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.offer(new LocationDistance(neighbor, newDist));
                }
            }
        }

        return distances;
    }

    private PathResult reconstructPath(Location source, Location destination, 
                                      Map<Location, Location> previous, double totalTime) {
        List<Location> path = new ArrayList<>();
        Location current = destination;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return new PathResult(path, totalTime);
    }

    private static class LocationDistance implements Comparable<LocationDistance> {
        Location location;
        double distance;

        LocationDistance(Location location, double distance) {
            this.location = location;
            this.distance = distance;
        }

        @Override
        public int compareTo(LocationDistance other) {
            return Double.compare(this.distance, other.distance);
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