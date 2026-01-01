 import java.util.*;

/**
 * Enhanced dispatch system with predictive analysis and performance tracking
 */
public class DispatchSystem {
    private EmergencyNetworkGraph network;
    private DijkstraPathfinder dijkstraPathfinder;
    private AStarPathfinder astarPathfinder;
    private PredictiveAnalyzer predictiveAnalyzer;
    private PerformanceMetrics metrics;
    
    private PriorityQueue<Incident> incidentQueue;
    private Map<String, ResponseUnit> responseUnits;
    private Map<String, Incident> activeIncidents;
    
    private PathfindingAlgorithm currentAlgorithm;
    public enum LogLevel {
        NONE,       // no console output
        SUMMARY,    // only high-level info
        VERBOSE     // full output (current behaviour)
    }

    private LogLevel logLevel = LogLevel.VERBOSE;

    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    private void logVerbose(String msg) {
        if (logLevel == LogLevel.VERBOSE) {
            System.out.println(msg);
        }
    }

    private void logSummary(String msg) {
        if (logLevel == LogLevel.VERBOSE || logLevel == LogLevel.SUMMARY) {
            System.out.println(msg);
        }
    }

    public enum PathfindingAlgorithm {
        DIJKSTRA, ASTAR
    }

    public DispatchSystem(EmergencyNetworkGraph network) {
        this.network = network;
        this.dijkstraPathfinder = new DijkstraPathfinder(network);
        this.astarPathfinder = new AStarPathfinder(network);
        this.predictiveAnalyzer = new PredictiveAnalyzer();
        this.metrics = new PerformanceMetrics();
        
        this.incidentQueue = new PriorityQueue<>();
        this.responseUnits = new HashMap<>();
        this.activeIncidents = new HashMap<>();
        
        this.currentAlgorithm = PathfindingAlgorithm.ASTAR;
    }

    /**
     * Register a response unit
     */
    public void registerUnit(ResponseUnit unit) {
        responseUnits.put(unit.getId(), unit);
    }

    /**
     * Report a new incident with predictive analysis
     */
    public void reportIncident(Incident incident) {
        incidentQueue.offer(incident);
        activeIncidents.put(incident.getId(), incident);
        
        predictiveAnalyzer.recordIncident(incident);
        
        logVerbose(" Incident reported: " + incident);
        
        dispatchNextIncident();
    }

    /**
     * Dispatch next incident with algorithm selection
     */
    public void dispatchNextIncident() {
        if (incidentQueue.isEmpty()) {
            return;
        }

        Incident incident = incidentQueue.peek();
        
        DispatchResult result = findBestDispatch(incident);
        
        if (result != null) {
            incidentQueue.poll();
            executeDispatch(result);
        } else {
            metrics.recordFailedDispatch(incident);
            logVerbose("  No available units for: " + incident.getId());
        }
    }

    /**
     * Find best dispatch considering multiple factors
     */
    private DispatchResult findBestDispatch(Incident incident) {
        ResponseUnit bestUnit = null;
        double bestScore = Double.POSITIVE_INFINITY;
        Object bestPath = null;
        String algorithmUsed = null;

        for (ResponseUnit unit : responseUnits.values()) {
            if (!unit.isAvailable() || !unit.canRespondTo(incident.getType())) {
                continue;
            }

            long startTime = System.nanoTime();
            Object path = findPath(unit.getCurrentLocation(), incident.getLocation());
            long endTime = System.nanoTime();

            if (path != null) {
                double responseTime = getPathTime(path);
                
                // Multi-criteria scoring: time + severity weight
                double score = responseTime / (incident.getSeverity() * 0.2 + 0.8);
                
                if (score < bestScore) {
                    bestScore = score;
                    bestUnit = unit;
                    bestPath = path;
                    algorithmUsed = currentAlgorithm.name();
                }

                metrics.recordAlgorithmTime(currentAlgorithm.name(), endTime - startTime);
            }
        }

        if (bestUnit != null) {
            return new DispatchResult(bestUnit, incident, bestPath, 
                                     getPathTime(bestPath), algorithmUsed);
        }

        return null;
    }

    /**
     * Execute a dispatch
     */
    private void executeDispatch(DispatchResult result) {
        result.unit.assignToIncident(result.incident);
        result.incident.setAssignedUnit(result.unit);
        result.incident.setStatus(Incident.IncidentStatus.ASSIGNED);

        metrics.recordDispatch(
            result.incident,
            result.unit,
            result.responseTime,
            getPathDistance(result.path),
            result.algorithm
        );

        logVerbose(String.format("DISPATCH: %s → %s", 
            result.unit.getCallSign(), result.incident.getId()));
        logVerbose("   Algorithm: " + result.algorithm);
        logVerbose("   ETA: " + String.format("%.2f", result.responseTime) + " minutes");
        logVerbose("   Path: " + formatPath(result.path));
    }

    /**
     * Resolve an incident
     */
    public void resolveIncident(String incidentId) {
        Incident incident = activeIncidents.remove(incidentId);
        if (incident != null) {
            ResponseUnit unit = incident.getAssignedUnit();
            if (unit != null) {
                unit.completeIncident();
                logVerbose(String.format(" Incident %s resolved. %s available.", 
                    incidentId, unit.getCallSign()));
            }
            incident.setStatus(Incident.IncidentStatus.RESOLVED);
            dispatchNextIncident();
        }
    }

    /**
     * Proactive resource pre-positioning based on predictions
     */
    public void repositionUnitsProactively() {
        logSummary("\n Running predictive analysis for resource positioning...");
        
        List<PredictiveAnalyzer.HotspotScore> hotspots = 
            predictiveAnalyzer.getTopHotspots(3);

        if (hotspots.isEmpty()) {
            logSummary("   No hotspots identified yet.");
            return;
        }

        logSummary("   Top hotspots identified:");
        for (PredictiveAnalyzer.HotspotScore hotspot : hotspots) {
            logSummary("   - " + hotspot);
            
            Location nearestCenter = findNearestDispatchCenter(hotspot.location);
            if (nearestCenter != null) {
                logSummary("     → Recommend positioning units at " + nearestCenter.getName());
            }
        }
    }

    /**
     * Find nearest dispatch center to a location
     */
    private Location findNearestDispatchCenter(Location target) {
        List<Location> centers = network.getDispatchCenters();
        if (centers.isEmpty()) return null;

        Location nearest = null;
        double minDist = Double.POSITIVE_INFINITY;

        for (Location center : centers) {
            Object path = findPath(center, target);
            if (path != null) {
                double dist = getPathTime(path);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = center;
                }
            }
        }

        return nearest;
    }

    /**
     * Switch pathfinding algorithm
     */
    public void setPathfindingAlgorithm(PathfindingAlgorithm algorithm) {
        this.currentAlgorithm = algorithm;
        logSummary("Pathfinding algorithm set to: " + algorithm);
    }

    /**
     * Find path using current algorithm
     */
    private Object findPath(Location from, Location to) {
        switch (currentAlgorithm) {
            case DIJKSTRA:
                return dijkstraPathfinder.findShortestPath(from, to);
            case ASTAR:
                return astarPathfinder.findShortestPath(from, to);
            default:
                return dijkstraPathfinder.findShortestPath(from, to);
        }
    }

    /**
     * Get path time from result object
     */
    private double getPathTime(Object path) {
        if (path instanceof DijkstraPathfinder.PathResult) {
            return ((DijkstraPathfinder.PathResult) path).getTotalTime();
        } else if (path instanceof AStarPathfinder.PathResult) {
            return ((AStarPathfinder.PathResult) path).getTotalTime();
        }
        return 0.0;
    }

    /**
     * Get path distance (approximation)
     */
    private double getPathDistance(Object path) {
        return getPathTime(path) * 1.2;
    }

    /**
     * Format path for display
     */
    private String formatPath(Object path) {
        return path.toString();
    }

    /**
     * Get comprehensive status report
     */
    public String getStatusReport() {
        int available = 0;
        int dispatched = 0;
        
        for (ResponseUnit unit : responseUnits.values()) {
            if (unit.isAvailable()) available++;
            else dispatched++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════╗\n");
        sb.append("║           SYSTEM STATUS REPORT                         ║\n");
        sb.append("╚════════════════════════════════════════════════════════╝\n\n");
        
        sb.append(String.format("Active Incidents:       %d\n", activeIncidents.size()));
        sb.append(String.format("Pending Incidents:      %d\n", incidentQueue.size()));
        sb.append(String.format("Available Units:        %d\n", available));
        sb.append(String.format("Dispatched Units:       %d\n", dispatched));
        sb.append(String.format("Network Locations:      %d\n", network.getLocationCount()));
        sb.append(String.format("Pathfinding Algorithm:  %s\n", currentAlgorithm));
        sb.append(String.format("Success Rate:           %.2f%%\n", metrics.getSuccessRate()));

        return sb.toString();
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public PredictiveAnalyzer getPredictiveAnalyzer() {
        return predictiveAnalyzer;
    }

    /**
     * Dispatch result holder
     */
    private static class DispatchResult {
        ResponseUnit unit;
        Incident incident;
        Object path;
        double responseTime;
        String algorithm;

        DispatchResult(ResponseUnit unit, Incident incident, Object path, 
                      double responseTime, String algorithm) {
            this.unit = unit;
            this.incident = incident;
            this.path = path;
            this.responseTime = responseTime;
            this.algorithm = algorithm;
        }
    }
}