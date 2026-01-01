import java.util.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

/**
 * Machine learning-inspired adaptive weight system
 * Learns from historical response times and adjusts edge weights dynamically
 */
public class AdaptiveWeightLearner {
    private Map<EdgeKey, WeightHistory> edgeHistories;
    private Map<EdgeKey, Double> learnedWeights;
    private double learningRate;
    private int windowSize;
    
    public AdaptiveWeightLearner(double learningRate, int windowSize) {
        this.edgeHistories = new HashMap<>();
        this.learnedWeights = new HashMap<>();
        this.learningRate = learningRate;
        this.windowSize = windowSize;
    }
    
    /**
     * Record actual travel time for an edge
     */
    public void recordActualTravelTime(Location from, Location to, 
                                       double actualTime, LocalDateTime timestamp) {
        EdgeKey key = new EdgeKey(from.getId(), to.getId());
        
        edgeHistories.putIfAbsent(key, new WeightHistory(windowSize));
        WeightHistory history = edgeHistories.get(key);
        
        history.addObservation(actualTime, timestamp);
        
        // Update learned weight using exponential moving average
        updateLearnedWeight(key, actualTime);
    }
    
    /**
     * Update learned weight using exponential moving average
     */
    private void updateLearnedWeight(EdgeKey key, double actualTime) {
        double currentWeight = learnedWeights.getOrDefault(key, actualTime);
        double newWeight = (learningRate * actualTime) + ((1 - learningRate) * currentWeight);
        learnedWeights.put(key, newWeight);
    }
    
    /**
     * Get adaptive congestion factor based on time patterns
     */
    public double getAdaptiveCongestionFactor(Location from, Location to, 
                                               LocalDateTime queryTime) {
        EdgeKey key = new EdgeKey(from.getId(), to.getId());
        WeightHistory history = edgeHistories.get(key);
        
        if (history == null || history.observations.isEmpty()) {
            return 1.0; // No data, return neutral factor
        }
        
        // Calculate time-based factor
        double timeOfDayFactor = calculateTimeOfDayFactor(history, queryTime);
        double dayOfWeekFactor = calculateDayOfWeekFactor(history, queryTime);
        double recentTrendFactor = calculateRecentTrendFactor(history);
        
        // Weighted combination
        return (timeOfDayFactor * 0.4 + dayOfWeekFactor * 0.3 + recentTrendFactor * 0.3);
    }
    
    /**
     * Calculate congestion factor based on time of day
     */
    private double calculateTimeOfDayFactor(WeightHistory history, LocalDateTime queryTime) {
        int queryHour = queryTime.getHour();
        List<Double> similarTimeObservations = new ArrayList<>();
        
        for (TravelObservation obs : history.observations) {
            int obsHour = obs.timestamp.getHour();
            if (Math.abs(obsHour - queryHour) <= 2) {
                similarTimeObservations.add(obs.actualTime);
            }
        }
        
        if (similarTimeObservations.isEmpty()) {
            return 1.0;
        }
        
        double avgSimilarTime = similarTimeObservations.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
            
        double overallAvg = history.observations.stream()
            .mapToDouble(obs -> obs.actualTime)
            .average()
            .orElse(1.0);
        
        return avgSimilarTime / Math.max(0.1, overallAvg);
    }
    
    /**
     * Calculate congestion factor based on day of week
     */
    private double calculateDayOfWeekFactor(WeightHistory history, LocalDateTime queryTime) {
        DayOfWeek queryDay = queryTime.getDayOfWeek();
        List<Double> sameDayObservations = new ArrayList<>();
        
        for (TravelObservation obs : history.observations) {
            if (obs.timestamp.getDayOfWeek() == queryDay) {
                sameDayObservations.add(obs.actualTime);
            }
        }
        
        if (sameDayObservations.isEmpty()) {
            return 1.0;
        }
        
        double avgSameDay = sameDayObservations.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
            
        double overallAvg = history.observations.stream()
            .mapToDouble(obs -> obs.actualTime)
            .average()
            .orElse(1.0);
        
        return avgSameDay / Math.max(0.1, overallAvg);
    }
    
    /**
     * Calculate recent trend factor (are times increasing or decreasing?)
     */
    private double calculateRecentTrendFactor(WeightHistory history) {
        if (history.observations.size() < 3) {
            return 1.0;
        }
        
        // Take last 5 observations
        int start = Math.max(0, history.observations.size() - 5);
        List<TravelObservation> recent = history.observations.subList(start, history.observations.size());
        
        // Calculate simple linear trend
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = recent.size();
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = recent.get(i).actualTime;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double avgTime = sumY / n;
        
        // If slope is positive (times increasing), increase factor
        // If slope is negative (times decreasing), decrease factor
        double trendFactor = 1.0 + (slope / avgTime) * 2.0;
        
        return Math.max(0.5, Math.min(2.0, trendFactor));
    }
    
    /**
     * Get predicted weight for an edge
     */
    public Double getPredictedWeight(Location from, Location to) {
        EdgeKey key = new EdgeKey(from.getId(), to.getId());
        return learnedWeights.get(key);
    }
    
    /**
     * Get learning statistics
     */
    public Map<String, Object> getLearningStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEdgesLearned", learnedWeights.size());
        stats.put("totalObservations", edgeHistories.values().stream()
            .mapToInt(h -> h.observations.size())
            .sum());
        stats.put("learningRate", learningRate);
        stats.put("windowSize", windowSize);
        
        // Calculate average prediction accuracy
        double avgAccuracy = calculateAverageAccuracy();
        stats.put("averageAccuracy", avgAccuracy);
        
        return stats;
    }
    
    /**
     * Calculate average prediction accuracy
     */
    private double calculateAverageAccuracy() {
        double totalError = 0.0;
        int count = 0;
        
        for (Map.Entry<EdgeKey, WeightHistory> entry : edgeHistories.entrySet()) {
            EdgeKey key = entry.getKey();
            WeightHistory history = entry.getValue();
            Double predicted = learnedWeights.get(key);
            
            if (predicted != null && !history.observations.isEmpty()) {
                double actual = history.observations.stream()
                    .mapToDouble(obs -> obs.actualTime)
                    .average()
                    .orElse(0.0);
                
                double error = Math.abs(predicted - actual) / actual;
                totalError += error;
                count++;
            }
        }
        
        return count > 0 ? (1.0 - (totalError / count)) * 100 : 0.0;
    }
    
    /**
     * Generate learning report
     */
    public String generateLearningReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║          ADAPTIVE LEARNING REPORT                        ║\n");
        sb.append("╚══════════════════════════════════════════════════════════╝\n\n");
        
        Map<String, Object> stats = getLearningStatistics();
        
        sb.append("LEARNING PARAMETERS:\n");
        sb.append("────────────────────────────────────────────────────────────\n");
        sb.append(String.format("Learning Rate:        %.3f\n", stats.get("learningRate")));
        sb.append(String.format("Window Size:          %d observations\n", stats.get("windowSize")));
        sb.append("\n");
        
        sb.append("LEARNING OUTCOMES:\n");
        sb.append("────────────────────────────────────────────────────────────\n");
        sb.append(String.format("Edges Learned:        %d\n", stats.get("totalEdgesLearned")));
        sb.append(String.format("Total Observations:   %d\n", stats.get("totalObservations")));
        sb.append(String.format("Prediction Accuracy:  %.2f%%\n", stats.get("averageAccuracy")));
        sb.append("\n");
        
        // Show top 5 most learned edges
        sb.append("TOP 5 MOST LEARNED ROUTES:\n");
        sb.append("────────────────────────────────────────────────────────────\n");
        
        List<Map.Entry<EdgeKey, WeightHistory>> sorted = new ArrayList<>(edgeHistories.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue().observations.size(), 
                                              a.getValue().observations.size()));
        
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            EdgeKey key = sorted.get(i).getKey();
            WeightHistory history = sorted.get(i).getValue();
            Double learned = learnedWeights.get(key);
            
            sb.append(String.format("%d. %s → %s\n", i + 1, key.fromId, key.toId));
            sb.append(String.format("   Observations: %d\n", history.observations.size()));
            if (learned != null) {
                sb.append(String.format("   Learned Weight: %.2f min\n", learned));
            }
            sb.append("\n");
        }
        
        sb.append("═══════════════════════════════════════════════════════════\n");
        return sb.toString();
    }
    
    /**
     * Edge key for HashMap
     */
    private static class EdgeKey {
        String fromId;
        String toId;
        
        EdgeKey(String fromId, String toId) {
            this.fromId = fromId;
            this.toId = toId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgeKey edgeKey = (EdgeKey) o;
            return fromId.equals(edgeKey.fromId) && toId.equals(edgeKey.toId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(fromId, toId);
        }
    }
    
    /**
     * Weight history for an edge
     */
    private static class WeightHistory {
        List<TravelObservation> observations;
        int maxSize;
        
        WeightHistory(int maxSize) {
            this.observations = new ArrayList<>();
            this.maxSize = maxSize;
        }
        
        void addObservation(double actualTime, LocalDateTime timestamp) {
            observations.add(new TravelObservation(actualTime, timestamp));
            
            // Keep only recent observations (sliding window)
            if (observations.size() > maxSize) {
                observations.remove(0);
            }
        }
    }
    
    /**
     * Single travel observation
     */
    private static class TravelObservation {
        double actualTime;
        LocalDateTime timestamp;
        
        TravelObservation(double actualTime, LocalDateTime timestamp) {
            this.actualTime = actualTime;
            this.timestamp = timestamp;
        }
    }
}