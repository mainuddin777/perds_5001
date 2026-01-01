import java.util.*;

/**
 * Graph-based representation of the emergency network
 * Uses adjacency list for efficient storage and traversal
 */
public class EmergencyNetworkGraph {
    private Map<Location, List<Edge>> adjacencyList;
    private Map<String, Location> locations;

    public EmergencyNetworkGraph() {
        this.adjacencyList = new HashMap<>();
        this.locations = new HashMap<>();
    }

    public void addLocation(Location location) {
        if (!locations.containsKey(location.getId())) {
            locations.put(location.getId(), location);
            adjacencyList.putIfAbsent(location, new ArrayList<>());
        }
    }

    public void removeLocation(String locationId) {
        Location location = locations.remove(locationId);
        if (location != null) {
            adjacencyList.remove(location);
            for (List<Edge> edges : adjacencyList.values()) {
                edges.removeIf(edge -> edge.getDestination().equals(location));
            }
        }
    }

    public void addConnection(Location loc1, Location loc2, double distance, double travelTime) {
        Edge edge1 = new Edge(loc1, loc2, distance, travelTime);
        Edge edge2 = new Edge(loc2, loc1, distance, travelTime);
        
        adjacencyList.get(loc1).add(edge1);
        adjacencyList.get(loc2).add(edge2);
    }

    public void updateCongestion(Location source, Location destination, double congestionFactor) {
        List<Edge> edges = adjacencyList.get(source);
        if (edges != null) {
            for (Edge edge : edges) {
                if (edge.getDestination().equals(destination)) {
                    edge.setCongestionFactor(congestionFactor);
                    break;
                }
            }
        }
    }

    public List<Edge> getNeighbors(Location location) {
        return adjacencyList.getOrDefault(location, new ArrayList<>());
    }

    public Location getLocation(String id) {
        return locations.get(id);
    }

    public Collection<Location> getAllLocations() {
        return locations.values();
    }

    public List<Location> getDispatchCenters() {
        List<Location> centers = new ArrayList<>();
        for (Location loc : locations.values()) {
            if (loc.getType() == Location.LocationType.DISPATCH_CENTER) {
                centers.add(loc);
            }
        }
        return centers;
    }

    public int getLocationCount() {
        return locations.size();
    }

    public int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count / 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Emergency Network Graph:\n");
        sb.append(String.format("Locations: %d, Connections: %d\n", getLocationCount(), getEdgeCount()));
        return sb.toString();
    }
}