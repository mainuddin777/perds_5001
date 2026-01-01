import java.time.LocalDateTime;

/**
 * Represents an emergency incident requiring response
 */
public class Incident implements Comparable<Incident> {
    private final String id;
    private final Location location;
    private final IncidentType type;
    private final int severity; // 1-5, where 5 is most severe
    private final LocalDateTime reportedTime;
    private IncidentStatus status;
    private ResponseUnit assignedUnit;

    public enum IncidentType {
        FIRE, MEDICAL, POLICE, HAZMAT, RESCUE
    }

    public enum IncidentStatus {
        REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED
    }

    public Incident(String id, Location location, IncidentType type, int severity) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.severity = Math.min(5, Math.max(1, severity));
        this.reportedTime = LocalDateTime.now();
        this.status = IncidentStatus.REPORTED;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public IncidentType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public LocalDateTime getReportedTime() {
        return reportedTime;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public ResponseUnit getAssignedUnit() {
        return assignedUnit;
    }

    public void setAssignedUnit(ResponseUnit unit) {
        this.assignedUnit = unit;
        if (unit != null) {
            this.status = IncidentStatus.ASSIGNED;
        }
    }

    public int getPriorityScore() {
        return severity * 10;
    }

    @Override
    public int compareTo(Incident other) {
        int priorityCompare = Integer.compare(other.severity, this.severity);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return this.reportedTime.compareTo(other.reportedTime);
    }

    @Override
    public String toString() {
        return String.format("Incident[%s]: %s at %s (Severity: %d, Status: %s)", 
            id, type, location.getName(), severity, status);
    }
}