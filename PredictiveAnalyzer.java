import java.util.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

/**
 * Predictive analysis system for forecasting high-demand areas
 */
public class PredictiveAnalyzer {
    private Map<Location, List<IncidentRecord>> historicalData;
    private Map<Location, HotspotScore> currentHotspots;

    public PredictiveAnalyzer() {
        this.historicalData = new HashMap<>();
        this.currentHotspots = new HashMap<>();
    }

    public void recordIncident(Incident incident) {
        Location loc = incident.getLocation();
        historicalData.putIfAbsent(loc, new ArrayList<>());
        
        IncidentRecord record = new IncidentRecord(
            incident.getType(),
            incident.getSeverity(),
            incident.getReportedTime()
        );
        
        historicalData.get(loc).add(record);
        updateHotspots();
    }

    public void updateHotspots() {
        currentHotspots.clear();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Location, List<IncidentRecord>> entry : historicalData.entrySet()) {
            Location location = entry.getKey();
            List<IncidentRecord> records = entry.getValue();

            double score = calculateHotspotScore(records, now);
            currentHotspots.put(location, new HotspotScore(location, score, records.size()));
        }
    }

    private double calculateHotspotScore(List<IncidentRecord> records, LocalDateTime now) {
        if (records.isEmpty()) return 0.0;

        double frequencyScore = 0.0;
        double severityScore = 0.0;
        double timeScore = 0.0;
        int recentCount = 0;

        for (IncidentRecord record : records) {
            long hoursAgo = java.time.Duration.between(record.timestamp, now).toHours();
            double timeFactor = Math.exp(-hoursAgo / 168.0);

            frequencyScore += timeFactor;
            severityScore += record.severity * timeFactor;
            
            int hourDiff = Math.abs(record.timestamp.getHour() - now.getHour());
            timeScore += (24 - hourDiff) / 24.0 * timeFactor;

            if (hoursAgo < 24) recentCount++;
        }

        frequencyScore = frequencyScore / Math.max(1, records.size());
        severityScore = severityScore / (5.0 * Math.max(1, records.size()));
        timeScore = timeScore / Math.max(1, records.size());

        double surgeFactor = recentCount > 3 ? 1.5 : 1.0;

        return (frequencyScore * 0.4 + severityScore * 0.4 + timeScore * 0.2) * surgeFactor * 100;
    }

    public List<HotspotScore> getTopHotspots(int n) {
        List<HotspotScore> hotspots = new ArrayList<>(currentHotspots.values());
        hotspots.sort((a, b) -> Double.compare(b.score, a.score));
        return hotspots.subList(0, Math.min(n, hotspots.size()));
    }

    public double predictIncidentProbability(Location location, int hoursAhead) {
        List<IncidentRecord> records = historicalData.getOrDefault(location, new ArrayList<>());
        if (records.isEmpty()) return 0.0;

        LocalDateTime targetTime = LocalDateTime.now().plusHours(hoursAhead);
        int targetHour = targetTime.getHour();
        DayOfWeek targetDay = targetTime.getDayOfWeek();

        int matchingTimeSlots = 0;
        for (IncidentRecord record : records) {
            if (Math.abs(record.timestamp.getHour() - targetHour) <= 2 &&
                record.timestamp.getDayOfWeek() == targetDay) {
                matchingTimeSlots++;
            }
        }

        double baseRate = (double) records.size() / Math.max(1, 
            java.time.Duration.between(records.get(0).timestamp, LocalDateTime.now()).toDays());
        double timePatternFactor = 1.0 + (matchingTimeSlots / Math.max(1.0, records.size()));

        return Math.min(1.0, baseRate * timePatternFactor);
    }

    public Map<Location, Integer> suggestResourceAllocation(int totalUnits) {
        Map<Location, Integer> allocation = new HashMap<>();
        List<HotspotScore> hotspots = getTopHotspots(totalUnits);

        double totalScore = hotspots.stream().mapToDouble(h -> h.score).sum();

        for (HotspotScore hotspot : hotspots) {
            int units = (int) Math.ceil((hotspot.score / totalScore) * totalUnits);
            allocation.put(hotspot.location, Math.max(1, units));
        }

        return allocation;
    }

    public String getAnalysisReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Predictive Analysis Report ===\n");
        sb.append(String.format("Total locations tracked: %d\n", historicalData.size()));
        sb.append(String.format("Total historical incidents: %d\n", 
            historicalData.values().stream().mapToInt(List::size).sum()));
        
        sb.append("\nTop 5 Hotspots:\n");
        List<HotspotScore> top = getTopHotspots(5);
        for (int i = 0; i < top.size(); i++) {
            HotspotScore hs = top.get(i);
            sb.append(String.format("%d. %s - Score: %.2f (Incidents: %d)\n", 
                i + 1, hs.location.getName(), hs.score, hs.incidentCount));
        }

        return sb.toString();
    }

    private static class IncidentRecord {
        Incident.IncidentType type;
        int severity;
        LocalDateTime timestamp;

        IncidentRecord(Incident.IncidentType type, int severity, LocalDateTime timestamp) {
            this.type = type;
            this.severity = severity;
            this.timestamp = timestamp;
        }
    }

    public static class HotspotScore {
        public final Location location;
        public final double score;
        public final int incidentCount;

        public HotspotScore(Location location, double score, int incidentCount) {
            this.location = location;
            this.score = score;
            this.incidentCount = incidentCount;
        }

        @Override
        public String toString() {
            return String.format("%s: %.2f (n=%d)", location.getName(), score, incidentCount);
        }
    }
}