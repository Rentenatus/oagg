/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import agg.util.XMLHelper;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import static org.testng.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance tests for the new XML serialization module.
 * These tests measure and compare the performance of the new DOM-based
 * serialization against the legacy XMLHelper implementation.
 */
public class PerformanceTest {
    
    private static final String BASELINE_DIR = "../test_xml/baseline/samples/";
    private static final int ITERATIONS = 5;
    
    private List<String> testFiles;
    private List<Long> legacyLoadTimes;
    private List<Long> legacySaveTimes;
    private List<Long> newLoadTimes;
    private List<Long> newSaveTimes;
    
    @BeforeClass
    public void setUp() throws Exception {
        testFiles = new ArrayList<>();
        testFiles.add("small_graph.ggx");
        testFiles.add("small_graph_layered.ggx");
        testFiles.add("medium_graph.ggx");
        testFiles.add("large_graph.ggx");
        
        legacyLoadTimes = new ArrayList<>();
        legacySaveTimes = new ArrayList<>();
        newLoadTimes = new ArrayList<>();
        newSaveTimes = new ArrayList<>();
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        // Print performance summary
        printPerformanceSummary();
    }
    
    private void printPerformanceSummary() {
        System.out.println("\n=== Performance Test Summary ===");
        System.out.println("Test Files: " + testFiles);
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();
        
        for (int i = 0; i < testFiles.size(); i++) {
            String filename = testFiles.get(i);
            System.out.println("File: " + filename);
            System.out.println("  Legacy Load Avg: " + 
                (legacyLoadTimes.size() > i ? legacyLoadTimes.get(i) + "ms" : "N/A"));
            System.out.println("  New Load Avg: " + 
                (newLoadTimes.size() > i ? newLoadTimes.get(i) + "ms" : "N/A"));
            System.out.println();
        }
    }
    
    /**
     * Measures legacy XMLHelper load performance
     */
    @Test
    public void testLegacyLoadPerformance() throws Exception {
        System.out.println("\n=== Testing Legacy Load Performance ===");
        
        for (String filename : testFiles) {
            File file = new File(BASELINE_DIR + filename);
            long totalTime = 0;
            
            for (int i = 0; i < ITERATIONS; i++) {
                XMLHelper helper = new XMLHelper();
                long start = System.currentTimeMillis();
                boolean loaded = helper.read_from_xml(file.getAbsolutePath());
                long end = System.currentTimeMillis();
                
                assertTrue(loaded, "Legacy helper should load: " + filename);
                totalTime += (end - start);
            }
            
            long avgTime = totalTime / ITERATIONS;
            legacyLoadTimes.add(avgTime);
            System.out.println(filename + ": " + avgTime + "ms (avg over " + ITERATIONS + " iterations)");
        }
    }
    
    /**
     * Measures new DOM-based load performance
     */
    @Test
    public void testNewLoadPerformance() throws Exception {
        System.out.println("\n=== Testing New Load Performance ===");
        
        for (String filename : testFiles) {
            File file = new File(BASELINE_DIR + filename);
            long totalTime = 0;
            
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
                long end = System.currentTimeMillis();
                
                assertNotNull(context, "New context should load: " + filename);
                assertNotNull(context.getDocument(), "Document should be loaded: " + filename);
                totalTime += (end - start);
            }
            
            long avgTime = totalTime / ITERATIONS;
            newLoadTimes.add(avgTime);
            System.out.println(filename + ": " + avgTime + "ms (avg over " + ITERATIONS + " iterations)");
        }
    }
    
    /**
     * Measures legacy XMLHelper save performance
     */
    @Test
    public void testLegacySavePerformance() throws Exception {
        System.out.println("\n=== Testing Legacy Save Performance ===");
        
        for (String filename : testFiles) {
            File inputFile = new File(BASELINE_DIR + filename);
            File outputFile = new File("target/perf-test/legacy_" + filename);
            outputFile.getParentFile().mkdirs();
            
            XMLHelper helper = new XMLHelper();
            helper.read_from_xml(inputFile.getAbsolutePath());
            
            long totalTime = 0;
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                boolean saved = helper.save_to_xml(outputFile.getAbsolutePath());
                long end = System.currentTimeMillis();
                
                assertTrue(saved, "Legacy helper should save: " + filename);
                totalTime += (end - start);
            }
            
            long avgTime = totalTime / ITERATIONS;
            legacySaveTimes.add(avgTime);
            System.out.println(filename + ": " + avgTime + "ms (avg over " + ITERATIONS + " iterations)");
            
            // Clean up
            outputFile.delete();
        }
    }
    
    /**
     * Measures new DOM-based save performance
     */
    @Test
    public void testNewSavePerformance() throws Exception {
        System.out.println("\n=== Testing New Save Performance ===");
        
        for (String filename : testFiles) {
            File inputFile = new File(BASELINE_DIR + filename);
            File outputFile = new File("target/perf-test/new_" + filename);
            outputFile.getParentFile().mkdirs();
            
            long totalTime = 0;
            for (int i = 0; i < ITERATIONS; i++) {
                DOMXMLDeserializerContext deserializer = new DOMXMLDeserializerContext(inputFile);
                DOMXMLSerializerContext serializer = new DOMXMLSerializerContext(deserializer.getDocument());
                
                long start = System.currentTimeMillis();
                String xml = serializer.toXMLString();
                
                // Write to file
                try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
                    writer.write(xml);
                }
                long end = System.currentTimeMillis();
                
                assertNotNull(xml, "XML should be generated: " + filename);
                totalTime += (end - start);
            }
            
            long avgTime = totalTime / ITERATIONS;
            newSaveTimes.add(avgTime);
            System.out.println(filename + ": " + avgTime + "ms (avg over " + ITERATIONS + " iterations)");
            
            // Clean up
            outputFile.delete();
        }
    }
    
    /**
     * Creates a simple performance benchmark and saves results
     */
    @Test
    public void testCreatePerformanceBaseline() throws Exception {
        System.out.println("\n=== Creating Performance Baseline ===");
        
        StringBuilder results = new StringBuilder();
        results.append("# XML Serialization Performance Baseline\n\n");
        results.append("Date: ").append(new java.util.Date()).append("\n\n");
        results.append("## Test Configuration\n\n");
        results.append("- Iterations: ").append(ITERATIONS).append("\n");
        results.append("- Java Version: ").append(System.getProperty("java.version")).append("\n\n");
        
        results.append("## Load Performance (ms)\n\n");
        results.append("| File | Legacy (XMLHelper) | New (DOM) |\n");
        results.append("|------|------------------|-----------|\n");
        
        for (int i = 0; i < testFiles.size(); i++) {
            String filename = testFiles.get(i);
            long legacyTime = legacyLoadTimes.size() > i ? legacyLoadTimes.get(i) : 0;
            long newTime = newLoadTimes.size() > i ? newLoadTimes.get(i) : 0;
            results.append("| ").append(filename).append(" | ")
                      .append(legacyTime).append(" | ").append(newTime).append(" |\n");
        }
        
        results.append("\n## Save Performance (ms)\n\n");
        results.append("| File | Legacy (XMLHelper) | New (DOM) |\n");
        results.append("|------|------------------|-----------|\n");
        
        for (int i = 0; i < testFiles.size(); i++) {
            String filename = testFiles.get(i);
            long legacyTime = legacySaveTimes.size() > i ? legacySaveTimes.get(i) : 0;
            long newTime = newSaveTimes.size() > i ? newSaveTimes.get(i) : 0;
            results.append("| ").append(filename).append(" | ")
                      .append(legacyTime).append(" | ").append(newTime).append(" |\n");
        }
        
        // Save results
        File resultsFile = new File("target/perf-test/performance-baseline.md");
        resultsFile.getParentFile().mkdirs();
        try (java.io.FileWriter writer = new java.io.FileWriter(resultsFile)) {
            writer.write(results.toString());
        }
        
        System.out.println("Performance baseline saved to: " + resultsFile.getAbsolutePath());
    }
}
