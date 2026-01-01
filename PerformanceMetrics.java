import java.util.*;

/**
 * Tracks and analyzes performance metrics for the dispatch system
 */
public class PerformanceMetrics {
    private List<DispatchRecord> dispatchHistory;
    private Map<String, Long> algorithmExecutionTimes;
    private int totalIncidents;
    private int successfulDispatches;
    private int failedDispatches;

    public PerformanceMetrics() {
        this.dispatchHistory = new ArrayList<>();
        this.algorithmExecutionTimes = new HashMap<>();
        this.totalIncidents = 0;
        this.successfulDispatches = 0;
        this.failedDispatches = 0;
    }

    /**
     * Record a dispatch event
     */
    public void recordDispatch(Incident incident, ResponseUnit unit, double responseTime, 
                              double pathDistance, String algorithm) {
        DispatchRecord record = new DispatchRecord(
            incident.getId(),
            incident.getSeverity(),
            unit.getId(),
            responseTime,
            pathDistance,
            algorithm,
            System.currentTimeMillis()
        );
        
        dispatchHistory.add(record);
        totalIncidents++;
        successfulDispatches++;
    }

    /**
     * Record a failed dispatch (no available unit)
     */
    public void recordFailedDispatch(Incident incident) {
        totalIncidents++;
        failedDispatches++;
    }

    /**
     * Record algorithm execution time
     */
    public void recordAlgorithmTime(String algorithm, long timeNanos) {
        algorithmExecutionTimes.merge(algorithm, timeNanos, Long::sum);
    }

    /**
     * Calculate average response time
     */
    public double getAverageResponseTime() {
        if (dispatchHistory.isEmpty()) return 0.0;
        return dispatchHistory.stream()
            .mapToDouble(r -> r.responseTime)
            .average()
            .orElse(0.0);
    }

    /**
     * Calculate average response time by severity
     */
    public Map<Integer, Double> getAverageResponseTimeBySeverity() {
        Map<Integer, List<Double>> bySeverity = new HashMap<>();
        
        for (DispatchRecord record : dispatchHistory) {
            bySeverity.putIfAbsent(record.severity, new ArrayList<>());
            bySeverity.get(record.severity).add(record.responseTime);
        }

        Map<Integer, Double> averages = new HashMap<>();
        for (Map.Entry<Integer, List<Double>> entry : bySeverity.entrySet()) {
            double avg = entry.getValue().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            averages.put(entry.getKey(), avg);
        }

        return averages;
    }

    /**
     * Get success rate (percentage of incidents successfully dispatched)
     */
    public double getSuccessRate() {
        if (totalIncidents == 0) return 0.0;
        return (double) successfulDispatches / totalIncidents * 100.0;
    }

    /**
     * Get algorithm performance comparison
     */
    public Map<String, AlgorithmStats> getAlgorithmComparison() {
        Map<String, List<DispatchRecord>> byAlgorithm = new HashMap<>();
        
        for (DispatchRecord record : dispatchHistory) {
            byAlgorithm.putIfAbsent(record.algorithm, new ArrayList<>());
            byAlgorithm.get(record.algorithm).add(record);
        }

        Map<String, AlgorithmStats> stats = new HashMap<>();
        for (Map.Entry<String, List<DispatchRecord>> entry : byAlgorithm.entrySet()) {
            String algo = entry.getKey();
            List<DispatchRecord> records = entry.getValue();
            
            double avgTime = records.stream()
                .mapToDouble(r -> r.responseTime)
                .average()
                .orElse(0.0);
            
            double avgDistance = records.stream()
                .mapToDouble(r -> r.pathDistance)
                .average()
                .orElse(0.0);
            
            long avgExecTime = algorithmExecutionTimes.getOrDefault(algo, 0L) / 
                              Math.max(1, records.size());

            stats.put(algo, new AlgorithmStats(algo, records.size(), avgTime, 
                                               avgDistance, avgExecTime));
        }

        return stats;
    }

    /**
     * Generate comprehensive performance report
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════╗\n");
        sb.append("║       PERFORMANCE METRICS REPORT                       ║\n");
        sb.append("╚════════════════════════════════════════════════════════╝\n\n");

        // Overall Statistics
        sb.append("OVERALL STATISTICS:\n");
        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append(String.format("Total Incidents:          %d\n", totalIncidents));
        sb.append(String.format("Successful Dispatches:    %d\n", successfulDispatches));
        sb.append(String.format("Failed Dispatches:        %d\n", failedDispatches));
        sb.append(String.format("Success Rate:             %.2f%%\n", getSuccessRate()));
        sb.append(String.format("Average Response Time:    %.2f minutes\n\n", getAverageResponseTime()));

        // Response Time by Severity
        sb.append("RESPONSE TIME BY SEVERITY:\n");
        sb.append("─────────────────────────────────────────────────────────\n");
        Map<Integer, Double> bySeverity = getAverageResponseTimeBySeverity();
        for (int severity = 5; severity >= 1; severity--) {
            if (bySeverity.containsKey(severity)) {
                sb.append(String.format("Severity %d:  %.2f minutes\n", 
                    severity, bySeverity.get(severity)));
            }
        }
        sb.append("\n");

        // Algorithm Comparison
        sb.append("ALGORITHM PERFORMANCE COMPARISON:\n");
        sb.append("─────────────────────────────────────────────────────────\n");
        Map<String, AlgorithmStats> algoStats = getAlgorithmComparison();
        for (AlgorithmStats stats : algoStats.values()) {
            sb.append(String.format("%s:\n", stats.algorithm));
            sb.append(String.format("  Dispatches:       %d\n", stats.dispatchCount));
            sb.append(String.format("  Avg Time:         %.2f min\n", stats.avgResponseTime));
            sb.append(String.format("  Avg Distance:     %.2f km\n", stats.avgDistance));
            sb.append(String.format("  Execution Time:   %.3f ms\n\n", stats.avgExecutionTime / 1_000_000.0));
        }

        // Recent Performance Trend
        if (dispatchHistory.size() >= 5) {
            sb.append("RECENT PERFORMANCE (Last 5 dispatches):\n");
            sb.append("─────────────────────────────────────────────────────────\n");
            List<DispatchRecord> recent = dispatchHistory.subList(
                Math.max(0, dispatchHistory.size() - 5), dispatchHistory.size()
            );
            for (DispatchRecord record : recent) {
                sb.append(String.format("Incident %s: %.2f min (Severity: %d)\n", 
                    record.incidentId, record.responseTime, record.severity));
            }
        }

        sb.append("\n═════════════════════════════════════════════════════════\n");
        return sb.toString();
    }

    /**
     * Export data for visualization (CSV format)
     */
    public String exportCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("IncidentID,Severity,UnitID,ResponseTime,PathDistance,Algorithm,Timestamp\n");
        
        for (DispatchRecord record : dispatchHistory) {
            sb.append(String.format("%s,%d,%s,%.2f,%.2f,%s,%d\n",
                record.incidentId,
                record.severity,
                record.unitId,
                record.responseTime,
                record.pathDistance,
                record.algorithm,
                record.timestamp
            ));
        }
        
        return sb.toString();
    }

    /**
     * Dispatch record for historical tracking
     */
    private static class DispatchRecord {
        String incidentId;
        int severity;
        String unitId;
        double responseTime;
        double pathDistance;
        String algorithm;
        long timestamp;

        DispatchRecord(String incidentId, int severity, String unitId, 
                      double responseTime, double pathDistance, 
                      String algorithm, long timestamp) {
            this.incidentId = incidentId;
            this.severity = severity;
            this.unitId = unitId;
            this.responseTime = responseTime;
            this.pathDistance = pathDistance;
            this.algorithm = algorithm;
            this.timestamp = timestamp;
        }
    }

    /**
     * Algorithm statistics
     */
    public static class AlgorithmStats {
        String algorithm;
        int dispatchCount;
        double avgResponseTime;
        double avgDistance;
        long avgExecutionTime;

        AlgorithmStats(String algorithm, int dispatchCount, double avgResponseTime,
                      double avgDistance, long avgExecutionTime) {
            this.algorithm = algorithm;
            this.dispatchCount = dispatchCount;
            this.avgResponseTime = avgResponseTime;
            this.avgDistance = avgDistance;
            this.avgExecutionTime = avgExecutionTime;
        }
    }
}