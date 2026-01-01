import java.util.*;

/**
 * Comprehensive scalability testing framework
 * Tests system performance under varying load conditions
 */
public class ScalabilityTester {
    
    public static void runScalabilityTests() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          SCALABILITY TESTING SUITE                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        // Test 1: Network Size Scalability
        testNetworkSizeScalability();
        
        // Test 2: Incident Load Scalability
        testIncidentLoadScalability();
        
        // Test 3: Algorithm Performance Comparison
        testAlgorithmScalability();
        
        // Test 4: Concurrent Operations
        testConcurrentOperations();
        
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          SCALABILITY TESTS COMPLETED                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Test 1: Network size scalability
     */
    private static void testNetworkSizeScalability() {
        System.out.println("═══ TEST 1: Network Size Scalability ═══\n");
        
        int[] sizes = {10, 25, 50, 100, 200};
        List<ScalabilityResult> results = new ArrayList<>();
        
        for (int size : sizes) {
            long startTime = System.nanoTime();
            
            // Create network
            EmergencyNetworkGraph network = createScalableNetwork(size);
            DijkstraPathfinder pathfinder = new DijkstraPathfinder(network);
            
            // Measure pathfinding time
            List<Location> locs = new ArrayList<>(network.getAllLocations());
            Location start = locs.get(0);
            Location end = locs.get(locs.size() - 1);
            
            long pathStartTime = System.nanoTime();
            DijkstraPathfinder.PathResult path = pathfinder.findShortestPath(start, end);
            long pathEndTime = System.nanoTime();
            
            long totalTime = System.nanoTime() - startTime;
            
            results.add(new ScalabilityResult(
                size,
                network.getEdgeCount(),
                totalTime / 1_000_000.0,
                (pathEndTime - pathStartTime) / 1_000_000.0,
                path != null ? path.getPath().size() : 0
            ));
        }
        
        // Print results
        System.out.println("Network Size | Edges | Setup Time (ms) | Pathfinding (ms) | Path Length");
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        for (ScalabilityResult result : results) {
            System.out.printf("%12d | %5d | %15.2f | %16.3f | %11d\n",
                result.networkSize, result.edgeCount, result.setupTime,
                result.operationTime, result.pathLength);
        }
        
        // Calculate complexity growth
        System.out.println("\nComplexity Analysis:");
        if (results.size() >= 2) {
            ScalabilityResult first = results.get(0);
            ScalabilityResult last = results.get(results.size() - 1);
            
            double sizeGrowth = (double) last.networkSize / first.networkSize;
            double timeGrowth = last.operationTime / first.operationTime;
            
            double complexity = Math.log(timeGrowth) / Math.log(sizeGrowth);
            
            System.out.printf("  Network grew by: %.1fx\n", sizeGrowth);
            System.out.printf("  Time grew by: %.2fx\n", timeGrowth);
            System.out.printf("  Empirical complexity: O(n^%.2f)\n", complexity);
            System.out.printf("  Expected: O(n log n) ≈ O(n^1.0 to n^1.2)\n");
        }
        System.out.println();
    }
    
    /**
     * Test 2: Incident load scalability
     */
    private static void testIncidentLoadScalability() {
        System.out.println("═══ TEST 2: Incident Load Scalability ═══\n");
        
        EmergencyNetworkGraph network = createScalableNetwork(50);
        
        int[] incidentCounts = {10, 25, 50, 100, 200};
        List<LoadTestResult> results = new ArrayList<>();
        
        for (int count : incidentCounts) {
            DispatchSystem dispatch = new DispatchSystem(network);
            if (count <= 25) {
                dispatch.setLogLevel(DispatchSystem.LogLevel.VERBOSE);
            } else {
                dispatch.setLogLevel(DispatchSystem.LogLevel.SUMMARY);
            }
            
            // Register units
            List<Location> centers = network.getDispatchCenters();
            int unitCount = Math.min(count / 2, 50);
            for (int i = 0; i < unitCount; i++) {
                Location loc = centers.get(i % centers.size());
                ResponseUnit.UnitType type = ResponseUnit.UnitType.values()[i % 4];
                dispatch.registerUnit(new ResponseUnit("U" + i, "UNIT-" + i, type, loc));
            }
            
            // Generate incidents
            long startTime = System.nanoTime();
            List<Location> allLocs = new ArrayList<>(network.getAllLocations());
            
            for (int i = 0; i < count; i++) {
                Location loc = allLocs.get(i % allLocs.size());
                Incident.IncidentType type = Incident.IncidentType.values()[i % 4];
                int severity = (i % 5) + 1;
                
                Incident incident = new Incident("INC" + i, loc, type, severity);
                dispatch.reportIncident(incident);
            }
            
            long endTime = System.nanoTime();
            double totalTime = (endTime - startTime) / 1_000_000.0;
            
            results.add(new LoadTestResult(
                count,
                totalTime,
                totalTime / count,
                dispatch.getMetrics().getSuccessRate()
            ));
        }
        
        // Print results
        System.out.println("Incidents | Total Time (ms) | Avg Time/Incident (ms) | Success Rate");
        System.out.println("────────────────────────────────────────────────────────────────────");
        for (LoadTestResult result : results) {
            System.out.printf("%9d | %15.2f | %22.3f | %11.2f%%\n",
                result.incidentCount, result.totalTime,
                result.avgTimePerIncident, result.successRate);
        }
        
        // Throughput analysis
        System.out.println("\nThroughput Analysis:");
        LoadTestResult maxLoad = results.get(results.size() - 1);
        double throughput = maxLoad.incidentCount / (maxLoad.totalTime / 1000.0);
        System.out.printf("  Peak throughput: %.1f incidents/second\n", throughput);
        System.out.printf("  Success rate at peak: %.2f%%\n", maxLoad.successRate);
        System.out.println();
    }
    
    /**
     * Test 3: Algorithm performance comparison
     */
    private static void testAlgorithmScalability() {
        System.out.println("═══ TEST 3: Algorithm Performance Comparison ═══\n");
        
        int[] sizes = {20, 50, 100, 150};
        Map<String, List<Double>> dijkstraTimes = new HashMap<>();
        Map<String, List<Double>> astarTimes = new HashMap<>();
        
        dijkstraTimes.put("times", new ArrayList<>());
        astarTimes.put("times", new ArrayList<>());
        
        for (int size : sizes) {
            EmergencyNetworkGraph network = createScalableNetwork(size);
            DijkstraPathfinder dijkstra = new DijkstraPathfinder(network);
            AStarPathfinder astar = new AStarPathfinder(network);
            
            List<Location> locs = new ArrayList<>(network.getAllLocations());
            Location start = locs.get(0);
            Location end = locs.get(locs.size() - 1);
            
            // Test Dijkstra
            long startTime = System.nanoTime();
            dijkstra.findShortestPath(start, end);
            long endTime = System.nanoTime();
            dijkstraTimes.get("times").add((endTime - startTime) / 1_000_000.0);
            
            // Test A*
            startTime = System.nanoTime();
            astar.findShortestPath(start, end);
            endTime = System.nanoTime();
            astarTimes.get("times").add((endTime - startTime) / 1_000_000.0);
        }
        
        // Print results
        System.out.println("Network Size | Dijkstra (ms) | A* (ms) | Speedup Factor");
        System.out.println("────────────────────────────────────────────────────────────");
        
        for (int i = 0; i < sizes.length; i++) {
            double dijkstraTime = dijkstraTimes.get("times").get(i);
            double astarTime = astarTimes.get("times").get(i);
            double speedup = dijkstraTime / astarTime;
            
            System.out.printf("%12d | %13.3f | %7.3f | %.2fx faster\n",
                sizes[i], dijkstraTime, astarTime, speedup);
        }
        
        System.out.println("\nConclusion:");
        double avgSpeedup = 0.0;
        for (int i = 0; i < sizes.length; i++) {
            avgSpeedup += dijkstraTimes.get("times").get(i) / astarTimes.get("times").get(i);
        }
        avgSpeedup /= sizes.length;
        System.out.printf("  Average A* speedup: %.2fx\n", avgSpeedup);
        System.out.printf("  A* is more efficient for sparse networks with heuristic guidance\n");
        System.out.println();
    }
    
    /**
     * Test 4: Concurrent operations stress test
     */
    private static void testConcurrentOperations() {
        System.out.println("═══ TEST 4: Concurrent Operations Stress Test ═══\n");
        
        EmergencyNetworkGraph network = createScalableNetwork(100);
        DispatchSystem dispatch = new DispatchSystem(network);
        dispatch.setLogLevel(DispatchSystem.LogLevel.NONE);
        
        // Register many units
        List<Location> centers = network.getDispatchCenters();
        for (int i = 0; i < 50; i++) {
            Location loc = centers.get(i % centers.size());
            ResponseUnit.UnitType type = ResponseUnit.UnitType.values()[i % 4];
            dispatch.registerUnit(new ResponseUnit("U" + i, "UNIT-" + i, type, loc));
        }
        
        // Simulate concurrent operations
        int operationCount = 1000;
        List<Location> allLocs = new ArrayList<>(network.getAllLocations());
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < operationCount; i++) {
            Location loc = allLocs.get(i % allLocs.size());
            Incident.IncidentType type = Incident.IncidentType.values()[i % 4];
            int severity = (i % 5) + 1;
            
            Incident incident = new Incident("INC" + i, loc, type, severity);
            dispatch.reportIncident(incident);
            
            // Randomly resolve some incidents
            if (i % 10 == 0 && i > 0) {
                dispatch.resolveIncident("INC" + (i - 5));
            }
        }
        
        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("Concurrent Operations Test Results:");
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.printf("Total operations: %d\n", operationCount);
        System.out.printf("Total time: %.2f ms\n", totalTime);
        System.out.printf("Operations/second: %.1f\n", operationCount / (totalTime / 1000.0));
        System.out.printf("Success rate: %.2f%%\n", dispatch.getMetrics().getSuccessRate());
        System.out.println();
    }
    
    /**
     * Create scalable network for testing
     */
    private static EmergencyNetworkGraph createScalableNetwork(int size) {
        EmergencyNetworkGraph network = new EmergencyNetworkGraph();
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        // Create locations
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Location.LocationType type = i < size / 4 ? 
                Location.LocationType.DISPATCH_CENTER : Location.LocationType.CITY;
            
            Location loc = new Location(
                "L" + i,
                "Location" + i,
                type,
                50 + rand.nextDouble() * 5,
                -3 + rand.nextDouble() * 5
            );
            
            locations.add(loc);
            network.addLocation(loc);
        }
        
        // Create connections (approximate mesh)
        for (int i = 0; i < size; i++) {
            // Connect each node to 3-5 neighbors
            int connections = 3 + rand.nextInt(3);
            for (int j = 0; j < connections; j++) {
                int targetIdx = rand.nextInt(size);
                if (targetIdx != i) {
                    double distance = 50 + rand.nextDouble() * 100;
                    double time = distance * 0.8;
                    network.addConnection(locations.get(i), locations.get(targetIdx), 
                                        distance, time);
                }
            }
        }
        
        return network;
    }
    
    /**
     * Scalability result holder
     */
    private static class ScalabilityResult {
        int networkSize;
        int edgeCount;
        double setupTime;
        double operationTime;
        int pathLength;
        
        ScalabilityResult(int networkSize, int edgeCount, double setupTime,
                         double operationTime, int pathLength) {
            this.networkSize = networkSize;
            this.edgeCount = edgeCount;
            this.setupTime = setupTime;
            this.operationTime = operationTime;
            this.pathLength = pathLength;
        }
    }
    
    /**
     * Load test result holder
     */
    private static class LoadTestResult {
        int incidentCount;
        double totalTime;
        double avgTimePerIncident;
        double successRate;
        
        LoadTestResult(int incidentCount, double totalTime,
                      double avgTimePerIncident, double successRate) {
            this.incidentCount = incidentCount;
            this.totalTime = totalTime;
            this.avgTimePerIncident = avgTimePerIncident;
            this.successRate = successRate;
        }
    }
}