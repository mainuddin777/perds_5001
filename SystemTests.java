import java.util.*;

public class SystemTests {

    private EmergencyNetworkGraph network;
    private Location london, manchester, birmingham;

    // ================= ASSERTIONS =================
    static void assertTrue(boolean v, String msg) { if (!v) fail(msg); }
    static void assertFalse(boolean v, String msg) { if (v) fail(msg); }
    static void assertNull(Object o, String msg) { if (o != null) fail(msg); }
    static void assertNotNull(Object o, String msg) { if (o == null) fail(msg); }

    static void assertEquals(Object a, Object b, String msg) {
        if (!Objects.equals(a, b))
            fail(msg + " (Expected: " + a + "  Got: " + b + ")");
    }

    static void assertEquals(double a, double b, double eps, String msg) {
        if (Math.abs(a - b) > eps)
            fail(msg + " (Expected: " + a + "  Got: " + b + ")");
    }

    static void fail(String msg) {
        throw new RuntimeException("❌ TEST FAILED → " + msg);
    }

    // ================= SETUP =================
    void setUp() {
        network = new EmergencyNetworkGraph();

        london = new Location("L1", "London", Location.LocationType.DISPATCH_CENTER, 51.5074, -0.1278);
        manchester = new Location("L2", "Manchester", Location.LocationType.CITY, 53.4808, -2.2426);
        birmingham = new Location("L3", "Birmingham", Location.LocationType.DISPATCH_CENTER, 52.4862, -1.8904);

        network.addLocation(london);
        network.addLocation(manchester);
        network.addLocation(birmingham);

        // Modified: Make indirect route cheaper to ensure 3-node path
        network.addConnection(london, birmingham, 163, 120);
        network.addConnection(birmingham, manchester, 135, 90);
        network.addConnection(london, manchester, 290, 250); // Increased to 250 to make indirect route better
    }

    // ================= NETWORK =================
    void testAddLocation() {
        assertEquals(3, network.getLocationCount(), "Should have 3 locations");
        assertNotNull(network.getLocation("L1"), "London exists");
        assertNotNull(network.getLocation("L2"), "Manchester exists");
    }

    void testAddConnection() {
        List<Edge> londonNeighbors = network.getNeighbors(london);
        assertEquals(2, londonNeighbors.size(), "London should have 2 connections");
    }

    void testRemoveLocation() {
        network.removeLocation("L2");
        assertEquals(2, network.getLocationCount(), "After removal");
        assertNull(network.getLocation("L2"), "Manchester removed");
    }

    void testUpdateCongestion() {
        network.updateCongestion(london, birmingham, 2.0);
        Edge e = network.getNeighbors(london).stream()
                .filter(x -> x.getDestination().equals(birmingham))
                .findFirst().orElse(null);
        assertNotNull(e, "Edge exists");
        assertEquals(2.0, e.getCongestionFactor(), 0.01, "Congestion updated");
    }

    // ================= PATHFINDING =================
    void testDijkstraPath() {
        DijkstraPathfinder p = new DijkstraPathfinder(network);
        DijkstraPathfinder.PathResult r = p.findShortestPath(london, manchester);
        assertNotNull(r, "Path exists");
        assertEquals(3, r.getPath().size(), "3 nodes");
        assertTrue(r.getTotalTime() > 0, "Time > 0");
        assertEquals(210.0, r.getTotalTime(), 1.0, "Total time should be 210 minutes");
    }

    void testAStarPath() {
        AStarPathfinder p = new AStarPathfinder(network);
        AStarPathfinder.PathResult r = p.findShortestPath(london, manchester);
        assertNotNull(r, "Path exists");
        assertTrue(r.getPath().size() >= 2, "Valid length");
    }

    void testNoPath() {
        Location iso = new Location("L4", "Isolated", Location.LocationType.CITY, 50, 0);
        network.addLocation(iso);
        DijkstraPathfinder p = new DijkstraPathfinder(network);
        assertNull(p.findShortestPath(london, iso), "No path");
    }

    // ================= INCIDENT =================
    void testIncidentPriority() {
        Incident high = new Incident("I1", london, Incident.IncidentType.MEDICAL, 5);
        Incident low = new Incident("I2", manchester, Incident.IncidentType.POLICE, 2);
        assertTrue(high.compareTo(low) < 0, "Priority works");
    }

    void testIncidentStatus() {
        Incident i = new Incident("I", london, Incident.IncidentType.FIRE, 4);
        assertEquals(Incident.IncidentStatus.REPORTED, i.getStatus(), "Reported");
        i.setStatus(Incident.IncidentStatus.ASSIGNED);
        assertEquals(Incident.IncidentStatus.ASSIGNED, i.getStatus(), "Assigned");
    }

    void testSeverityBounds() {
        Incident high = new Incident("I1", london, Incident.IncidentType.MEDICAL, 10);
        Incident low = new Incident("I2", london, Incident.IncidentType.FIRE, -5);
        assertEquals(5, high.getSeverity(), "Max 5");
        assertEquals(1, low.getSeverity(), "Min 1");
    }

    // ================= RESPONSE UNITS =================
    void testUnitAvailability() {
        ResponseUnit u = new ResponseUnit("U1", "A1", ResponseUnit.UnitType.AMBULANCE, london);
        assertTrue(u.isAvailable(), "Initially available");
        u.assignToIncident(new Incident("I", manchester, Incident.IncidentType.MEDICAL, 3));
        assertFalse(u.isAvailable(), "Unavailable");
    }

    void testUnitCapability() {
        ResponseUnit amb = new ResponseUnit("U1", "A1", ResponseUnit.UnitType.AMBULANCE, london);
        ResponseUnit fire = new ResponseUnit("U2", "F1", ResponseUnit.UnitType.FIRE_TRUCK, london);
        assertTrue(amb.canRespondTo(Incident.IncidentType.MEDICAL), "Amb ok");
        assertFalse(amb.canRespondTo(Incident.IncidentType.FIRE), "Amb no fire");
        assertTrue(fire.canRespondTo(Incident.IncidentType.FIRE), "Fire ok");
    }

    // ================= DISPATCH =================
    void testBasicDispatch() {
        DispatchSystem d = new DispatchSystem(network);
        ResponseUnit u = new ResponseUnit("U1", "A1", ResponseUnit.UnitType.AMBULANCE, london);
        d.registerUnit(u);
        Incident i = new Incident("I", birmingham, Incident.IncidentType.MEDICAL, 4);
        d.reportIncident(i);
        assertFalse(u.isAvailable(), "Dispatched");
        assertEquals(u, i.getAssignedUnit(), "Assigned");
    }

    void testResolution() {
        DispatchSystem d = new DispatchSystem(network);
        ResponseUnit u = new ResponseUnit("U1", "A1", ResponseUnit.UnitType.AMBULANCE, london);
        d.registerUnit(u);
        Incident i = new Incident("I", birmingham, Incident.IncidentType.MEDICAL, 4);
        d.reportIncident(i);
        d.resolveIncident("I");
        assertTrue(u.isAvailable(), "Free again");
        assertEquals(Incident.IncidentStatus.RESOLVED, i.getStatus(), "Resolved");
    }

    // ================= RUNNER =================
    public static void main(String[] args) {
        SystemTests t = new SystemTests();
        int passed = 0;
        int total = 14;

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║            SYSTEM TESTS - PERDS                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        try {
            System.out.print("Running testAddLocation... ");
            t.setUp(); t.testAddLocation(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testAddConnection... ");
            t.setUp(); t.testAddConnection(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testRemoveLocation... ");
            t.setUp(); t.testRemoveLocation(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testUpdateCongestion... ");
            t.setUp(); t.testUpdateCongestion(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testDijkstraPath... ");
            t.setUp(); t.testDijkstraPath(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testAStarPath... ");
            t.setUp(); t.testAStarPath(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testNoPath... ");
            t.setUp(); t.testNoPath(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testIncidentPriority... ");
            t.setUp(); t.testIncidentPriority(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testIncidentStatus... ");
            t.setUp(); t.testIncidentStatus(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testSeverityBounds... ");
            t.setUp(); t.testSeverityBounds(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testUnitAvailability... ");
            t.setUp(); t.testUnitAvailability(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testUnitCapability... ");
            t.setUp(); t.testUnitCapability(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testBasicDispatch... ");
            t.setUp(); t.testBasicDispatch(); passed++;
            System.out.println(" PASSED");

            System.out.print("Running testResolution... ");
            t.setUp(); t.testResolution(); passed++;
            System.out.println(" PASSED");

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println(String.format("║   ALL TESTS PASSED (%d/%d tests)                     ║", passed, total));
            System.out.println("╚════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            System.out.println("❌ FAILED");
            System.err.println("\n" + e.getMessage());
            System.out.println(String.format("\n Tests completed: %d/%d passed", passed, total));
        }
    }
}