package agg.refactoring;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import agg.util.XMLHelper;

/**
 * Performance benchmark baseline for XML serialization.
 * This class measures load and save times for .ggx files to establish
 * performance metrics before refactoring.
 * 
 * Part of Phase 1: Preparation - Step 1.5: Performance Benchmarking
 */
public class BenchmarkBaseline {

    private static final String BASELINE_SAMPLES_DIR = "test_xml/resources/baseline/samples/";
    private static final String BASELINE_ACTUAL_DIR = "test_xml/resources/baseline/actual/";
    private static final String ALT_BASELINE_SAMPLES_DIR = "D:/git_oagg/test_xml/resources/baseline/samples/";
    private static final String ALT_BASELINE_ACTUAL_DIR = "D:/git_oagg/test_xml/resources/baseline/actual/";
    
    private static final String[] SAMPLE_FILES = TestBaseline.getSampleFiles();
    
    // Benchmark results
    private static Map<String, Long> loadTimes = new HashMap<>();
    private static Map<String, Long> saveTimes = new HashMap<>();
    private static Map<String, Long> fileSizes = new HashMap<>();
    
    // Number of iterations for each test
    private static final int ITERATIONS = 5;

    public static void main(String[] args) {
        System.out.println("=== AGG XML Serialization Performance Benchmark ===");
        System.out.println("Phase 1 - Step 1.5: Performance Benchmarking");
        System.out.println("Using: agg.util.XMLHelper (real implementation)");
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();

        // Run benchmarks
        runLoadBenchmarks();
        runSaveBenchmarks();
        
        // Print results
        printBenchmarkResults();
        
        // Save results to file
        saveResultsToFile();
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
     * Get the output path for benchmarks
     */
    private static String getOutputPath(String filename) {
        String relativePath = BASELINE_ACTUAL_DIR + "benchmark_" + filename;
        String absolutePath = ALT_BASELINE_ACTUAL_DIR + "benchmark_" + filename;
        
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
     * Run load performance benchmarks using XMLHelper
     */
    private static void runLoadBenchmarks() {
        System.out.println("--- Running Load Benchmarks (XMLHelper.read_from_xml) ---");
        System.out.println("Iterations per file: " + ITERATIONS);
        System.out.println();
        
        for (String filename : SAMPLE_FILES) {
            String filepath = getFilePath(filename);
            File file = new File(filepath);
            
            if (!file.exists()) {
                System.err.println("WARN: File not found: " + filepath);
                continue;
            }
            
            fileSizes.put(filename, file.length());
            long totalTime = 0;
            
            // Measure average load time over multiple iterations
            for (int i = 0; i < ITERATIONS; i++) {
                long startTime = System.currentTimeMillis();
                
                try {
                    XMLHelper helper = new XMLHelper();
                    boolean loaded = helper.read_from_xml(filepath);
                    
                    if (!loaded) {
                        System.err.println("Error loading " + filename + " in iteration " + i);
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error reading " + filename + ": " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("  Caused by: " + e.getCause().getMessage());
                    }
                    break;
                }
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                totalTime += elapsedTime;
            }
            
            long avgTimeMs = totalTime / ITERATIONS;
            loadTimes.put(filename, avgTimeMs);
            
            System.out.println("Benchmark: " + filename + " (" + formatSize(fileSizes.get(filename)) + ")");
            System.out.println("  Average load time: " + avgTimeMs + " ms");
            System.out.println();
        }
    }

    /**
     * Run save performance benchmarks using XMLHelper
     */
    private static void runSaveBenchmarks() {
        System.out.println("--- Running Save Benchmarks (XMLHelper.save_to_xml) ---");
        System.out.println("Iterations per file: " + ITERATIONS);
        System.out.println();
        
        for (String filename : SAMPLE_FILES) {
            String sourcePath = getFilePath(filename);
            String outputPath = getOutputPath(filename);
            File sourceFile = new File(sourcePath);
            
            if (!sourceFile.exists()) {
                System.err.println("WARN: File not found: " + sourcePath);
                continue;
            }
            
            long totalTime = 0;
            
            // Measure average save time over multiple iterations
            for (int i = 0; i < ITERATIONS; i++) {
                long startTime = System.currentTimeMillis();
                
                try {
                    XMLHelper helper = new XMLHelper();
                    
                    // Load the file first
                    if (!helper.read_from_xml(sourcePath)) {
                        System.err.println("Error loading " + filename + " for save benchmark");
                        break;
                    }
                    
                    // Save to temp file
                    boolean saved = helper.save_to_xml(outputPath + "." + i);
                    
                    if (!saved) {
                        System.err.println("Error saving " + filename + " in iteration " + i);
                        break;
                    }
                    
                    // Clean up temp file
                    new File(outputPath + "." + i).delete();
                    
                } catch (Exception e) {
                    System.err.println("Error writing " + filename + ": " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("  Caused by: " + e.getCause().getMessage());
                    }
                    break;
                }
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                totalTime += elapsedTime;
            }
            
            long avgTimeMs = totalTime / ITERATIONS;
            saveTimes.put(filename, avgTimeMs);
            
            System.out.println("Benchmark: " + filename + " (" + formatSize(fileSizes.get(filename)) + ")");
            System.out.println("  Average save time: " + avgTimeMs + " ms");
            System.out.println();
        }
        
        // Clean up any remaining temp files
        for (String filename : SAMPLE_FILES) {
            String outputPath = getOutputPath(filename);
            for (int i = 0; i < ITERATIONS; i++) {
                new File(outputPath + "." + i).delete();
            }
        }
    }

    /**
     * Print benchmark results
     */
    private static void printBenchmarkResults() {
        System.out.println("=== Benchmark Results ===");
        System.out.println();
        
        System.out.println("| File | Size | Avg Load Time (ms) | Avg Save Time (ms) |");
        System.out.println("|------|------|---------------------|---------------------|");
        
        for (String filename : SAMPLE_FILES) {
            Long size = fileSizes.get(filename);
            Long loadTime = loadTimes.get(filename);
            Long saveTime = saveTimes.get(filename);
            
            if (size != null && loadTime != null && saveTime != null) {
                System.out.println(String.format("| %-20s | %-10s | %-19d | %-19d |", 
                    filename, 
                    formatSize(size),
                    loadTime,
                    saveTime));
            }
        }
        
        System.out.println();
        System.out.println("Performance Notes:");
        System.out.println("- These are baseline measurements using XMLHelper.read_from_xml and save_to_xml");
        System.out.println("- Target after refactoring: No significant degradation (> 200% of baseline)");
        System.out.println();
    }

    /**
     * Save benchmark results to file
     */
    private static void saveResultsToFile() {
        try {
            File resultsFile = new File("docs/refactoring/baseline-performance.md");
            java.nio.file.Files.createDirectories(resultsFile.getParentFile().toPath());
            
            StringBuilder sb = new StringBuilder();
            sb.append("# Baseline Performance Measurements\n\n");
            sb.append("Generated: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n\n");
            sb.append("## Environment\n\n");
            sb.append("- Java Version: ").append(System.getProperty("java.version")).append("\n");
            sb.append("- OS: ").append(System.getProperty("os.name")).append("\n");
            sb.append("- Iterations: ").append(ITERATIONS).append("\n\n");
            
            sb.append("## Results\n\n");
            sb.append("| File | Size | Avg Load (ms) | Avg Save (ms) |\n");
            sb.append("|------|------|---------------|---------------|\n");
            
            for (String filename : SAMPLE_FILES) {
                Long size = fileSizes.get(filename);
                Long loadTime = loadTimes.get(filename);
                Long saveTime = saveTimes.get(filename);
                
                if (size != null && loadTime != null && saveTime != null) {
                    sb.append(String.format("| %-20s | %-10s | %-14d | %-14d |%n", 
                        filename, 
                        formatSize(size),
                        loadTime,
                        saveTime));
                }
            }
            
            sb.append("\n");
            sb.append("## Notes\n\n");
            sb.append("- Baseline measurements using XMLHelper.read_from_xml and save_to_xml\n");
            sb.append("- After refactoring, compare new implementation times against these values\n");
            sb.append("- Target: No significant degradation (> 200% of baseline)\n");
            
            java.nio.file.Files.write(resultsFile.toPath(), sb.toString().getBytes());
            System.out.println("Benchmark results saved to: " + resultsFile.getPath());
            
        } catch (Exception e) {
            System.err.println("Error saving benchmark results: " + e.getMessage());
        }
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
}
