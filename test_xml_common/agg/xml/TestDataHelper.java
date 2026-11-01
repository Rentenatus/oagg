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

    /** Base directory for .ggx sample files, relative to the test working directory (assets_test_xml). */
    public static final String BASELINE_DIR = "../test_xml/baseline/samples/";

    /** Directory for preserved legacy XML test fixtures (.ggx, .rsx, .cpx). */
    public static final String LEGACY_DIR = "test_agg/legacy/";

    /** Absolute fallback for legacy directory. */
    public static final String ALT_LEGACY_DIR = "D:/git_oagg/assets_test_xml/test_agg/legacy/";

    /** All sample .ggx file names (directed variants first, then undirected counterparts). */
    public static final String[] SAMPLE_FILES = {
        "small_graph.ggx",
        "small_graph_layered.ggx",
        "medium_graph.ggx",
        "large_graph.ggx",
        "small_graph_undirected.ggx",
        "small_graph_layered_undirected.ggx",
        "medium_graph_undirected.ggx",
        "large_graph_undirected.ggx",
        "small_graph_noTG.ggx",
        "small_graph_layered_noTG.ggx",
        "small_graph_undirected_noTG.ggx",
        "small_graph_layered_undirected_noTG.ggx"
    };

    /** All legacy .ggx test fixture file names (directed variants first, then undirected counterparts). */
    public static final String[] LEGACY_GGX_FILES = {
        "basic_graph_attrs.ggx",
        "composite_all.ggx",
        "constraints.ggx",
        "match.ggx",
        "rule_nac_pac.ggx",
        "rule_scheme.ggx",
        "rule_sequence.ggx",
        "small_graph.ggx",
        "small_graph_layered.ggx",
        "medium_graph.ggx",
        "large_graph.ggx",
        "basic_graph_attrs_undirected.ggx",
        "composite_all_undirected.ggx",
        "constraints_undirected.ggx",
        "match_undirected.ggx",
        "rule_nac_pac_undirected.ggx",
        "rule_scheme_undirected.ggx",
        "rule_sequence_undirected.ggx",
        "small_graph_undirected.ggx",
        "small_graph_layered_undirected.ggx",
        "medium_graph_undirected.ggx",
        "large_graph_undirected.ggx",
        "basic_graph_attrs_noTG.ggx",
        "composite_all_noTG.ggx",
        "constraints_noTG.ggx",
        "match_noTG.ggx",
        "rule_nac_pac_noTG.ggx",
        "rule_scheme_noTG.ggx",
        "rule_sequence_noTG.ggx",
        "small_graph_noTG.ggx",
        "small_graph_layered_noTG.ggx",
        "basic_graph_attrs_undirected_noTG.ggx",
        "composite_all_undirected_noTG.ggx",
        "constraints_undirected_noTG.ggx",
        "match_undirected_noTG.ggx",
        "rule_nac_pac_undirected_noTG.ggx",
        "rule_scheme_undirected_noTG.ggx",
        "rule_sequence_undirected_noTG.ggx",
        "small_graph_undirected_noTG.ggx",
        "small_graph_layered_undirected_noTG.ggx"
    };

    /** All legacy non-.ggx test fixture file names. */
    public static final String[] LEGACY_OTHER_FILES = {
        "appl_rule_sequence.rsx",
        "conflicts_deps.cpx"
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
     * Resolves a legacy test fixture file name relative to the legacy directory.
     *
     * @param filename The legacy file name (e.g. "basic_graph_attrs.ggx")
     * @return The resolved File
     */
    public static File resolveLegacy(String filename) {
        File relative = new File(LEGACY_DIR + filename);
        if (relative.exists()) {
            return relative;
        }
        return new File(ALT_LEGACY_DIR + filename);
    }

    /**
     * Checks that a file exists; throws SkipException if not.
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
     * Checks that all legacy fixture files exist; throws SkipException if any
     * is missing.
     *
     * @throws SkipException if any legacy file does not exist
     */
    public static void requireAllLegacy() {
        for (String filename : LEGACY_GGX_FILES) {
            File file = resolveLegacy(filename);
            if (!file.exists()) {
                throw new SkipException("Legacy test data file missing: " + file.getAbsolutePath());
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

    /**
     * Returns the array of legacy .ggx fixture files.
     *
     * @return Array of legacy File objects
     */
    public static File[] getLegacyGgxFiles() {
        File[] files = new File[LEGACY_GGX_FILES.length];
        for (int i = 0; i < LEGACY_GGX_FILES.length; i++) {
            files[i] = resolveLegacy(LEGACY_GGX_FILES[i]);
        }
        return files;
    }
}
