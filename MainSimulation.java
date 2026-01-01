import java.time.LocalDateTime;

/**
 * Advanced simulation demonstrating all system features
 * INCLUDING: Adaptive Learning and Scalability Testing
 */
public class MainSimulation {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  PREDICTIVE EMERGENCY RESPONSE DISPATCH SYSTEM (PERDS)  ║");
        System.out.println("║                 Advanced Simulation                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        // Phase 1: System Setup
        System.out.println("═══ PHASE 1: System Initialization ═══\n");
        EmergencyNetworkGraph network = createComprehensiveNetwork();
        DispatchSystem dispatch = new DispatchSystem(network);
        AdaptiveWeightLearner adaptiveLearner = new AdaptiveWeightLearner(0.3, 50);
        registerResponseUnits(dispatch, network);
        
        System.out.println(dispatch.getStatusReport());
        
        // Phase 2: Initial Incident Response
        System.out.println("\n═══ PHASE 2: Initial Incident Response ═══\n");
        simulateInitialIncidents(dispatch, network);
        
        // Phase 3: Algorithm Comparison
        System.out.println("\n═══ PHASE 3: Algorithm Performance Comparison ═══\n");
        compareAlgorithms(dispatch, network);
        
        // Phase 4: Adaptive Learning System
        System.out.println("\n═══ PHASE 4: Adaptive Learning System ═══\n");
        demonstrateAdaptiveLearning(dispatch, network, adaptiveLearner);
        
        // Phase 5: Apply Learned Weights
        System.out.println("\n═══ PHASE 5: Applying Learned Patterns ═══\n");
        applyLearnedWeights(network, adaptiveLearner);
        
        // Phase 6: Dynamic Network Updates
        System.out.println("\n═══ PHASE 6: Dynamic Network Updates ═══\n");
        simulateDynamicChanges(network, dispatch);
        
        // Phase 7: Operations with Adaptive Weights
        System.out.println("\n═══ PHASE 7: Operations with Adaptive Routing ═══\n");
        simulateWithAdaptiveWeights(dispatch, network, adaptiveLearner);
        
        // Phase 8: Predictive Analysis
        System.out.println("\n═══ PHASE 8: Predictive Analysis ═══\n");
        dispatch.repositionUnitsProactively();
        System.out.println("\n" + dispatch.getPredictiveAnalyzer().getAnalysisReport());
        
        // Phase 9: High-Load Scenario
        System.out.println("\n═══ PHASE 9: High-Load Stress Test ═══\n");
        stressTest(dispatch, network);
        
        // Phase 10: Scalability Testing
        System.out.println("\n═══ PHASE 10: Scalability Testing ═══\n");
        ScalabilityTester.runScalabilityTests();
        
        // Phase 11: Final Performance Reports
        System.out.println("\n═══ PHASE 11: Final Performance Analysis ═══");
        System.out.println(adaptiveLearner.generateLearningReport());
        System.out.println(dispatch.getMetrics().generateReport());
        
        // Export data
        System.out.println("\n═══ Data Export ═══");
        System.out.println("CSV data available for visualization:");
        String csvData = dispatch.getMetrics().exportCSV();
        String[] lines = csvData.split("\n");
        if (lines.length > 0) {
            System.out.println(lines[0] + "...");
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              Simulation Completed Successfully          ║");
        System.out.println("║    Features: Standard + Adaptive Learning + Scalability  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Demonstrate adaptive learning
     */
    private static void demonstrateAdaptiveLearning(DispatchSystem dispatch,
                                                     EmergencyNetworkGraph network,
                                                     AdaptiveWeightLearner learner) {
        System.out.println("📚 Training adaptive weight system...\n");
        
        Location london = network.getLocation("L1");
        Location manchester = network.getLocation("L2");
        Location birmingham = network.getLocation("L3");
        
        // Simulate learning from historical data
        System.out.println("Recording historical travel patterns:");
        
        // Morning rush hour (slower)
        for (int i = 0; i < 10; i++) {
            LocalDateTime morning = LocalDateTime.now().withHour(8);
            learner.recordActualTravelTime(london, birmingham, 145.0, morning);
            learner.recordActualTravelTime(birmingham, manchester, 105.0, morning);
            System.out.print(".");
        }
        
        // Afternoon (normal)
        for (int i = 0; i < 10; i++) {
            LocalDateTime afternoon = LocalDateTime.now().withHour(14);
            learner.recordActualTravelTime(london, birmingham, 120.0, afternoon);
            learner.recordActualTravelTime(birmingham, manchester, 90.0, afternoon);
            System.out.print(".");
        }
        
        // Night (faster)
        for (int i = 0; i < 10; i++) {
            LocalDateTime night = LocalDateTime.now().withHour(22);
            learner.recordActualTravelTime(london, birmingham, 100.0, night);
            learner.recordActualTravelTime(birmingham, manchester, 75.0, night);
            System.out.print(".");
        }
        
        System.out.println(" Done!\n");
        
        // Show predictions
        System.out.println("Learned Patterns:");
        LocalDateTime testMorning = LocalDateTime.now().withHour(8);
        LocalDateTime testNight = LocalDateTime.now().withHour(22);
        
        double morningFactor = learner.getAdaptiveCongestionFactor(london, birmingham, testMorning);
        double nightFactor = learner.getAdaptiveCongestionFactor(london, birmingham, testNight);
        
        System.out.printf("  Morning congestion factor: %.2fx (slower)\n", morningFactor);
        System.out.printf("  Night congestion factor: %.2fx (faster)\n", nightFactor);
        System.out.println();
    }
    
    /**
     * Apply learned weights to network
     */
    private static void applyLearnedWeights(EmergencyNetworkGraph network,
                                            AdaptiveWeightLearner learner) {
        System.out.println("🔧 Applying learned weights to network...\n");
        
        Location london = network.getLocation("L1");
        Location birmingham = network.getLocation("L3");
        
        LocalDateTime currentTime = LocalDateTime.now();
        double adaptiveFactor = learner.getAdaptiveCongestionFactor(
            london, birmingham, currentTime);
        
        network.updateCongestion(london, birmingham, adaptiveFactor);
        network.updateCongestion(birmingham, london, adaptiveFactor);
        
        System.out.printf("✓ Updated London↔Birmingham route with factor: %.2fx\n", adaptiveFactor);
        System.out.println("  (Based on current time: " + currentTime.getHour() + ":00)");
        System.out.println();
    }
    
    /**
     * Simulate with adaptive weights
     */
    private static void simulateWithAdaptiveWeights(DispatchSystem dispatch,
                                                     EmergencyNetworkGraph network,
                                                     AdaptiveWeightLearner learner) {
        System.out.println("🚨 Processing incidents with adaptive routing...\n");
        
        Location leeds = network.getLocation("L4");
        Location liverpool = network.getLocation("L5");
        
        // Create incidents
        dispatch.reportIncident(new Incident("INC-ADV-001", leeds, 
            Incident.IncidentType.MEDICAL, 5));
        System.out.println();
        
        dispatch.reportIncident(new Incident("INC-ADV-002", liverpool, 
            Incident.IncidentType.FIRE, 4));
        System.out.println();
        
        // Simulate learning from these dispatches
        Location manchester = network.getLocation("L2");
        learner.recordActualTravelTime(manchester, leeds, 52.0, LocalDateTime.now());
        learner.recordActualTravelTime(manchester, liverpool, 48.0, LocalDateTime.now());
        
        // Resolve
        dispatch.resolveIncident("INC-ADV-001");
        dispatch.resolveIncident("INC-ADV-002");
    }
    
    /**
     * Create a comprehensive network with multiple cities
     */
    private static EmergencyNetworkGraph createComprehensiveNetwork() {
        EmergencyNetworkGraph network = new EmergencyNetworkGraph();
        
        // Major cities with real coordinates
        Location london = new Location("L1", "London", Location.LocationType.DISPATCH_CENTER, 51.5074, -0.1278);
        Location manchester = new Location("L2", "Manchester", Location.LocationType.DISPATCH_CENTER, 53.4808, -2.2426);
        Location birmingham = new Location("L3", "Birmingham", Location.LocationType.DISPATCH_CENTER, 52.4862, -1.8904);
        Location leeds = new Location("L4", "Leeds", Location.LocationType.CITY, 53.8008, -1.5491);
        Location liverpool = new Location("L5", "Liverpool", Location.LocationType.CITY, 53.4084, -2.9916);
        Location bristol = new Location("L6", "Bristol", Location.LocationType.DISPATCH_CENTER, 51.4545, -2.5879);
        Location sheffield = new Location("L7", "Sheffield", Location.LocationType.CITY, 53.3811, -1.4701);
        Location nottingham = new Location("L8", "Nottingham", Location.LocationType.CITY, 52.9548, -1.1581);
        
        // Add all locations to network
        network.addLocation(london);
        network.addLocation(manchester);
        network.addLocation(birmingham);
        network.addLocation(leeds);
        network.addLocation(liverpool);
        network.addLocation(bristol);
        network.addLocation(sheffield);
        network.addLocation(nottingham);
        
        // Create comprehensive network connections (distance in km, travel time in minutes)
        network.addConnection(london, birmingham, 163, 120);
        network.addConnection(london, bristol, 172, 130);
        network.addConnection(birmingham, manchester, 135, 90);
        network.addConnection(birmingham, nottingham, 75, 55);
        network.addConnection(birmingham, bristol, 145, 100);
        network.addConnection(manchester, leeds, 64, 50);
        network.addConnection(manchester, liverpool, 56, 45);
        network.addConnection(manchester, sheffield, 61, 48);
        network.addConnection(leeds, liverpool, 116, 80);
        network.addConnection(leeds, sheffield, 52, 42);
        network.addConnection(sheffield, nottingham, 56, 45);
        network.addConnection(nottingham, birmingham, 75, 55);
        
        System.out.println("✓ Network created: 8 locations, " + network.getEdgeCount() + " connections");
        return network;
    }
    
    /**
     * Register diverse response units across dispatch centers
     */
    private static void registerResponseUnits(DispatchSystem dispatch, 
                                             EmergencyNetworkGraph network) {
        Location london = network.getLocation("L1");
        Location manchester = network.getLocation("L2");
        Location birmingham = network.getLocation("L3");
        Location bristol = network.getLocation("L6");
        
        // London units - 3 units
        dispatch.registerUnit(new ResponseUnit("U1", "LON-A1", ResponseUnit.UnitType.AMBULANCE, london));
        dispatch.registerUnit(new ResponseUnit("U2", "LON-F1", ResponseUnit.UnitType.FIRE_TRUCK, london));
        dispatch.registerUnit(new ResponseUnit("U3", "LON-P1", ResponseUnit.UnitType.POLICE_CAR, london));
        
        // Manchester units - 2 units
        dispatch.registerUnit(new ResponseUnit("U4", "MAN-A1", ResponseUnit.UnitType.AMBULANCE, manchester));
        dispatch.registerUnit(new ResponseUnit("U5", "MAN-F1", ResponseUnit.UnitType.FIRE_TRUCK, manchester));
        
        // Birmingham units - 3 units
        dispatch.registerUnit(new ResponseUnit("U6", "BIR-A1", ResponseUnit.UnitType.AMBULANCE, birmingham));
        dispatch.registerUnit(new ResponseUnit("U7", "BIR-P1", ResponseUnit.UnitType.POLICE_CAR, birmingham));
        dispatch.registerUnit(new ResponseUnit("U8", "BIR-H1", ResponseUnit.UnitType.HAZMAT_TEAM, birmingham));
        
        // Bristol units - 2 units
        dispatch.registerUnit(new ResponseUnit("U9", "BRI-F1", ResponseUnit.UnitType.FIRE_TRUCK, bristol));
        dispatch.registerUnit(new ResponseUnit("U10", "BRI-A1", ResponseUnit.UnitType.AMBULANCE, bristol));
        
        System.out.println("✓ Registered 10 response units across 4 dispatch centers\n");
    }
    
    /**
     * Simulate initial set of incidents
     */
    private static void simulateInitialIncidents(DispatchSystem dispatch, 
                                                EmergencyNetworkGraph network) {
        Location leeds = network.getLocation("L4");
        Location liverpool = network.getLocation("L5");
        Location sheffield = network.getLocation("L7");
        Location nottingham = network.getLocation("L8");
        
        // Report multiple incidents with varying severity
        dispatch.reportIncident(new Incident("INC001", leeds, Incident.IncidentType.MEDICAL, 5));
        System.out.println();
        
        dispatch.reportIncident(new Incident("INC002", liverpool, Incident.IncidentType.FIRE, 4));
        System.out.println();
        
        dispatch.reportIncident(new Incident("INC003", sheffield, Incident.IncidentType.POLICE, 3));
        System.out.println();
        
        dispatch.reportIncident(new Incident("INC004", nottingham, Incident.IncidentType.MEDICAL, 4));
        System.out.println();
        
        // Simulate time passing and incidents being resolved
        System.out.println("⏰ Time passes... resolving incidents\n");
        dispatch.resolveIncident("INC001");
        dispatch.resolveIncident("INC002");
    }
    
    /**
     * Compare Dijkstra vs A* performance
     */
    private static void compareAlgorithms(DispatchSystem dispatch, 
                                         EmergencyNetworkGraph network) {
        System.out.println("Testing Dijkstra's Algorithm:");
        dispatch.setPathfindingAlgorithm(DispatchSystem.PathfindingAlgorithm.DIJKSTRA);
        
        Location leeds = network.getLocation("L4");
        dispatch.reportIncident(new Incident("INC005", leeds, Incident.IncidentType.FIRE, 3));
        System.out.println();
        
        System.out.println("Testing A* Algorithm:");
        dispatch.setPathfindingAlgorithm(DispatchSystem.PathfindingAlgorithm.ASTAR);
        
        Location liverpool = network.getLocation("L5");
        dispatch.reportIncident(new Incident("INC006", liverpool, Incident.IncidentType.MEDICAL, 4));
        System.out.println();
        
        // Resolve incidents to free up units
        dispatch.resolveIncident("INC005");
        dispatch.resolveIncident("INC006");
    }
    
    /**
     * Simulate dynamic network changes (traffic congestion)
     */
    private static void simulateDynamicChanges(EmergencyNetworkGraph network, 
                                              DispatchSystem dispatch) {
        System.out.println("⚠️  Simulating traffic congestion on Manchester-Leeds route");
        
        Location manchester = network.getLocation("L2");
        Location leeds = network.getLocation("L4");
        
        // Update congestion factor (2.5x normal travel time)
        network.updateCongestion(manchester, leeds, 2.5);
        network.updateCongestion(leeds, manchester, 2.5);
        
        System.out.println("✓ Congestion updated. System will adapt routing.\n");
        
        // Test incident on affected route
        dispatch.reportIncident(new Incident("INC007", leeds, Incident.IncidentType.MEDICAL, 5));
        System.out.println();
        
        dispatch.resolveIncident("INC007");
    }
    
    /**
     * High-load stress test with multiple simultaneous incidents
     */
    private static void stressTest(DispatchSystem dispatch, 
                                   EmergencyNetworkGraph network) {
        System.out.println("⚡ Simulating emergency surge (multiple simultaneous incidents)\n");
        
        Location[] locations = {
            network.getLocation("L4"), // Leeds
            network.getLocation("L5"), // Liverpool
            network.getLocation("L7"), // Sheffield
            network.getLocation("L8")  // Nottingham
        };
        
        Incident.IncidentType[] types = {
            Incident.IncidentType.MEDICAL,
            Incident.IncidentType.FIRE,
            Incident.IncidentType.POLICE,
            Incident.IncidentType.MEDICAL
        };
        
        // Create multiple incidents simultaneously
        for (int i = 0; i < 4; i++) {
            String id = String.format("INC%03d", 100 + i);
            Incident incident = new Incident(id, locations[i], types[i], 5 - i);
            dispatch.reportIncident(incident);
            System.out.println();
        }
        
        // Resolve all stress test incidents
        System.out.println("\n⏰ Resolving stress test incidents...\n");
        for (int i = 0; i < 4; i++) {
            String id = String.format("INC%03d", 100 + i);
            dispatch.resolveIncident(id);
        }
    }
}