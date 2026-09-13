/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml;

import org.testng.SkipException;

import java.io.File;

/**
 * Helper class for test data file resolution and skip-on-missing.
 *
 * <p>Tests should use {@link #requireFile(String)} or {@link #requireFile(File)}
 * instead of silent {@code if (!file.exists()) return;} patterns. When a test
 * data file is missing, the test is explicitly skipped with a clear message
 * rather than silently passing without testing anything.</p>
 */
public final class TestDataHelper {

    /** Base directory for .ggx sample files. */
    public static final String BASELINE_DIR = "../test_xml/baseline/samples/";

    /** All sample .ggx file names. */
    public static final String[] SAMPLE_FILES = {
        "small_graph.ggx",
        "small_graph_layered.ggx",
        "medium_graph.ggx",
        "large_graph.ggx"
    };

    private TestDataHelper() {
    }

    /**
     * Resolves a sample file name relative to the baseline directory.
     *
     * @param filename The sample file name (e.g. "small_graph.ggx")
     * @return The resolved File
     */
    public static File resolveSample(String filename) {
        return new File(BASELINE_DIR + filename);
    }

    /**
     * Checks that a file exists; throws SkipException if not.
     * Use this at the start of test methods that depend on a specific file.
     *
     * @param file The file to check
     * @throws SkipException if the file does not exist
     */
    public static void requireFile(File file) {
        if (!file.exists()) {
            throw new SkipException("Test data file missing: " + file.getAbsolutePath());
        }
    }

    /**
     * Checks that a sample file exists; throws SkipException if not.
     *
     * @param filename The sample file name
     * @throws SkipException if the file does not exist
     */
    public static void requireFile(String filename) {
        requireFile(resolveSample(filename));
    }

    /**
     * Checks that all sample files exist; throws SkipException if any is missing.
     * Use this in @BeforeClass to skip an entire test class when data is unavailable.
     *
     * @throws SkipException if any sample file does not exist
     */
    public static void requireAllSamples() {
        for (String filename : SAMPLE_FILES) {
            File file = resolveSample(filename);
            if (!file.exists()) {
                throw new SkipException("Test data file missing: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Returns the array of sample files.
     *
     * @return Array of sample File objects
     */
    public static File[] getSampleFiles() {
        File[] files = new File[SAMPLE_FILES.length];
        for (int i = 0; i < SAMPLE_FILES.length; i++) {
            files[i] = resolveSample(SAMPLE_FILES[i]);
        }
        return files;
    }
}
