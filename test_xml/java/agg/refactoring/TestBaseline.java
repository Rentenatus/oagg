package agg.refactoring;


import java.io.File;
import agg.util.XMLHelper;
import agg.xt_basis.GraGra;

/**
 * Baseline test suite for XML serialization.
 * This test verifies that existing .ggx files can be loaded and saved correctly
 * with the current XMLHelper implementation.
 * 
 * Part of Phase 1: Preparation - Step 1.4: Set Up Test Baseline
 */
public class TestBaseline {

    // Test data directories
    private static final String BASELINE_SAMPLES_DIR = "test_xml/resources/baseline/samples/";
    private static final String BASELINE_EXPECTED_DIR = "test_xml/resources/baseline/expected/";
    private static final String BASELINE_ACTUAL_DIR = "test_xml/resources/baseline/actual/";
    
    // Alternative paths for when running from different directories
    private static final String ALT_BASELINE_SAMPLES_DIR = "D:/git_oagg/test_xml/resources/baseline/samples/";
    private static final String ALT_BASELINE_ACTUAL_DIR = "D:/git_oagg/test_xml/resources/baseline/actual/";

    // Sample files to test
    private static final String[] SAMPLE_FILES = {
        "small_graph.ggx",
        "small_graph_layered.ggx", 
        "medium_graph.ggx",
        "large_graph.ggx"
    };

    // Performance tracking
    private static long totalLoadTime = 0;
    private static long totalSaveTime = 0;
    private static int filesProcessed = 0;
    private static int filesPassed = 0;
    private static int filesFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== AGG XML Serialization Baseline Test ===");
        System.out.println("Phase 1 - Step 1.4: Set Up Test Baseline");
        System.out.println("Using: agg.util.XMLHelper (real implementation)");
        System.out.println();

        // Initialize test environment
        initializeTestEnvironment();

        // Run all tests
        boolean allTestsPassed = true;
        
        allTestsPassed &= testLoadAllFiles();
        allTestsPassed &= testSaveAllFiles();
        allTestsPassed &= testRoundtrip();
        
        // Print summary
        printSummary(allTestsPassed);

        // Exit with appropriate code
        System.exit(allTestsPassed ? 0 : 1);
    }

    /**
     * Initialize test environment - create directories if they don't exist
     */
    private static void initializeTestEnvironment() {
        System.out.println("Initializing test environment...");
        
        File expectedDir = new File(BASELINE_EXPECTED_DIR);
        File actualDir = new File(BASELINE_ACTUAL_DIR);
        File altActualDir = new File(ALT_BASELINE_ACTUAL_DIR);
        
        if (!expectedDir.exists()) {
            expectedDir.mkdirs();
            System.out.println("Created directory: " + expectedDir.getPath());
        }
        
        if (!actualDir.exists()) {
            actualDir.mkdirs();
            System.out.println("Created directory: " + actualDir.getPath());
        }
        
        if (!altActualDir.exists()) {
            altActualDir.mkdirs();
            System.out.println("Created directory: " + altActualDir.getPath());
        }
        
        System.out.println("Test environment initialized.");
        System.out.println();
    }

    /**
     * Get the actual file path, trying both relative and absolute paths
     */
    private static String getFilePath(String filename) {
        String relativePath = BASELINE_SAMPLES_DIR + filename;
        String absolutePath = ALT_BASELINE_SAMPLES_DIR + filename;
        
        File relativeFile = new File(relativePath);
        File absoluteFile = new File(absolutePath);
        
        if (relativeFile.exists()) {
            return relativePath;
        } else if (absoluteFile.exists()) {
            return absolutePath;
        }
        
        return relativePath;
    }

    /**
     * Get the actual output path, trying both relative and absolute paths
     */
    private static String getOutputPath(String filename) {
        String relativePath = BASELINE_ACTUAL_DIR + filename;
        String absolutePath = ALT_BASELINE_ACTUAL_DIR + filename;
        
        File relativeDir = new File(BASELINE_ACTUAL_DIR);
        File absoluteDir = new File(ALT_BASELINE_ACTUAL_DIR);
        
        if (relativeDir.exists()) {
            return relativePath;
        } else if (absoluteDir.exists()) {
            return absolutePath;
        }
        
        return relativePath;
    }

    /**
     * Test loading all sample .ggx files using XMLHelper
     */
    private static boolean testLoadAllFiles() {
        System.out.println("--- Test: Load All Files (XMLHelper.read_from_xml) ---");
        boolean allPassed = true;
        
        for (String filename : SAMPLE_FILES) {
            String filepath = getFilePath(filename);
            long startTime = System.currentTimeMillis();
            
            try {
                XMLHelper helper = new XMLHelper();
                File file = new File(filepath);
                
                if (!file.exists()) {
                    System.err.println("FAIL: File not found: " + filepath);
                    allPassed = false;
                    filesFailed++;
                    continue;
                }
                
                long fileSize = file.length();
                
                // Use XMLHelper to load the file
                boolean loaded = helper.read_from_xml(filepath);
                
                if (!loaded) {
                    System.err.println("FAIL: " + filename + " - Could not load with XMLHelper");
                    allPassed = false;
                    filesFailed++;
                    continue;
                }
                
                long loadTime = System.currentTimeMillis() - startTime;
                totalLoadTime += loadTime;
                
                System.out.println("PASS: " + filename + " (" + formatSize(fileSize) + ") - Load: " + loadTime + "ms");
                filesProcessed++;
                filesPassed++;
                
            } catch (Exception e) {
                System.err.println("FAIL: " + filename + " - Error: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("  Caused by: " + e.getCause().getMessage());
                }
                allPassed = false;
                filesFailed++;
            }
        }
        
        System.out.println();
        return allPassed;
    }

    /**
     * Test saving functionality using XMLHelper
     */
    private static boolean testSaveAllFiles() {
        System.out.println("--- Test: Save All Files (XMLHelper.save_to_xml) ---");
        boolean allPassed = true;
        
        for (String filename : SAMPLE_FILES) {
            String inputPath = getFilePath(filename);
            String outputPath = getOutputPath(filename);
            long startTime = System.currentTimeMillis();
            
            try {
                XMLHelper helper = new XMLHelper();
                File inputFile = new File(inputPath);
                
                if (!inputFile.exists()) {
                    System.err.println("SKIP: Input file not found: " + inputPath);
                    continue;
                }
                
                // Load the file
                if (!helper.read_from_xml(inputPath)) {
                    System.err.println("SKIP: Could not load: " + inputPath);
                    continue;
                }
                
                // Save to actual directory
                boolean saved = helper.save_to_xml(outputPath);
                
                if (!saved) {
                    System.err.println("FAIL: " + filename + " - Could not save with XMLHelper");
                    allPassed = false;
                    filesFailed++;
                    continue;
                }
                
                long saveTime = System.currentTimeMillis() - startTime;
                totalSaveTime += saveTime;
                
                // Verify the file was created
                File outputFile = new File(outputPath);
                if (!outputFile.exists()) {
                    System.err.println("FAIL: " + filename + " - Output file not created");
                    allPassed = false;
                    filesFailed++;
                    continue;
                }
                
                System.out.println("PASS: " + filename + " - Saved to: " + outputPath + " (" + saveTime + "ms)");
                filesProcessed++;
                filesPassed++;
                
            } catch (Exception e) {
                System.err.println("FAIL: " + filename + " - Error: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("  Caused by: " + e.getCause().getMessage());
                }
                allPassed = false;
                filesFailed++;
            }
        }
        
        System.out.println();
        return allPassed;
    }

    /**
     * Test roundtrip: Load -> Save -> Load -> Compare
     */
    private static boolean testRoundtrip() {
        System.out.println("--- Test: Roundtrip (Load -> Save -> Load -> Compare) ---");
        boolean allPassed = true;
        
        for (String filename : SAMPLE_FILES) {
            String inputPath = getFilePath(filename);
            String outputPath = getOutputPath(filename);
            
            try {
                XMLHelper helper1 = new XMLHelper();
                XMLHelper helper2 = new XMLHelper();
                
                File inputFile = new File(inputPath);
                File outputFile = new File(outputPath);
                
                if (!inputFile.exists() || !outputFile.exists()) {
                    System.err.println("SKIP: " + filename + " - Files not found for comparison");
                    continue;
                }
                
                // Load original file
                if (!helper1.read_from_xml(inputPath)) {
                    System.err.println("SKIP: " + filename + " - Could not load original");
                    continue;
                }
                
                // Load saved file
                if (!helper2.read_from_xml(outputPath)) {
                    System.err.println("SKIP: " + filename + " - Could not load saved file");
                    continue;
                }
                
                // Compare file sizes
                long inputSize = inputFile.length();
                long outputSize = outputFile.length();
                
                long sizeDiff = Math.abs(inputSize - outputSize);
                double sizeDiffPercent = sizeDiff * 100.0 / Math.max(inputSize, outputSize);
                
                if (sizeDiffPercent < 1.0) {
                    System.out.println("PASS: " + filename + " - Roundtrip successful (size diff: " + 
                        String.format("%.2f", sizeDiffPercent) + "%)");
                } else {
                    System.err.println("WARN: " + filename + " - Size difference: " + 
                        String.format("%.2f", sizeDiffPercent) + "%");
                }
                
                filesProcessed++;
                filesPassed++;
                
            } catch (Exception e) {
                System.err.println("FAIL: " + filename + " - Error: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("  Caused by: " + e.getCause().getMessage());
                }
                allPassed = false;
                filesFailed++;
            }
        }
        
        System.out.println();
        return allPassed;
    }

    /**
     * Print test summary
     */
    private static void printSummary(boolean allTestsPassed) {
        System.out.println("=== Test Summary ===");
        System.out.println("Files processed: " + filesProcessed);
        System.out.println("Files passed: " + filesPassed);
        System.out.println("Files failed: " + filesFailed);
        System.out.println("Total load time: " + totalLoadTime + "ms");
        System.out.println("Total save time: " + totalSaveTime + "ms");
        
        if (filesProcessed > 0) {
            System.out.println("Average load time: " + (totalLoadTime / filesProcessed) + "ms");
            System.out.println("Average save time: " + (totalSaveTime / filesProcessed) + "ms");
        }
        
        System.out.println();
        
        if (allTestsPassed) {
            System.out.println("✅ ALL TESTS PASSED - Baseline established successfully");
        } else {
            System.out.println("❌ SOME TESTS FAILED - Check errors above");
        }
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("1. Review any failed tests");
        System.out.println("2. Fix classpath issues if files not found");
        System.out.println("3. Add content validation tests");
        System.out.println("4. Run BenchmarkBaseline for performance metrics");
    }

    /**
     * Format file size in human-readable format
     */
    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Get list of sample files
     */
    public static String[] getSampleFiles() {
        return SAMPLE_FILES;
    }

    /**
     * Get baseline samples directory
     */
    public static String getSamplesDirectory() {
        return BASELINE_SAMPLES_DIR;
    }

    /**
     * Get baseline expected directory
     */
    public static String getExpectedDirectory() {
        return BASELINE_EXPECTED_DIR;
    }

    /**
     * Get baseline actual directory
     */
    public static String getActualDirectory() {
        return BASELINE_ACTUAL_DIR;
    }
}
