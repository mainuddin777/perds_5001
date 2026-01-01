public class ResponseUnit {
    private final String id;
    private final String callSign;
    private final UnitType type;
    private Location currentLocation;
    private UnitStatus status;
    private Incident currentIncident;

    public enum UnitType {
        AMBULANCE, FIRE_TRUCK, POLICE_CAR, HAZMAT_TEAM, RESCUE_HELICOPTER
    }

    public enum UnitStatus {
        AVAILABLE, DISPATCHED, ON_SCENE, RETURNING
    }

    public ResponseUnit(String id, String callSign, UnitType type, Location startLocation) {
        this.id = id;
        this.callSign = callSign;
        this.type = type;
        this.currentLocation = startLocation;
        this.status = UnitStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public String getCallSign() {
        return callSign;
    }

    public UnitType getType() {
        return type;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public UnitStatus getStatus() {
        return status;
    }

    public void setStatus(UnitStatus status) {
        this.status = status;
    }

    public Incident getCurrentIncident() {
        return currentIncident;
    }

    public void assignToIncident(Incident incident) {
        this.currentIncident = incident;
        this.status = UnitStatus.DISPATCHED;
    }

    public void completeIncident() {
        this.currentIncident = null;
        this.status = UnitStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == UnitStatus.AVAILABLE;
    }

    public boolean canRespondTo(Incident.IncidentType incidentType) {
        switch (type) {
            case AMBULANCE:
                return incidentType == Incident.IncidentType.MEDICAL;
            case FIRE_TRUCK:
                return incidentType == Incident.IncidentType.FIRE;
            case POLICE_CAR:
                return incidentType == Incident.IncidentType.POLICE;
            case HAZMAT_TEAM:
                return incidentType == Incident.IncidentType.HAZMAT;
            case RESCUE_HELICOPTER:
                return true; // Can respond to any emergency
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s [%s] at %s - %s", 
            callSign, type, currentLocation.getName(), status);
    }
}