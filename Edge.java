public class Edge {
    private final Location source;
    private final Location destination;
    private double distance; // in kilometers
    private double travelTime; // in minutes
    private double congestionFactor; // 1.0 = normal, >1.0 = congested

    public Edge(Location source, Location destination, double distance, double travelTime) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.travelTime = travelTime;
        this.congestionFactor = 1.0;
    }

    public Location getSource() {
        return source;
    }

    public Location getDestination() {
        return destination;
    }

    public double getDistance() {
        return distance;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public double getCongestionFactor() {
        return congestionFactor;
    }

    public void setCongestionFactor(double congestionFactor) {
        this.congestionFactor = Math.max(1.0, congestionFactor);
    }

    public double getEffectiveTravelTime() {
        return travelTime * congestionFactor;
    }

    public double getWeight() {
        return getEffectiveTravelTime();
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%.2f km, %.2f min, congestion: %.2fx)", 
            source.getName(), destination.getName(), distance, travelTime, congestionFactor);
    }
}