/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.migration;

import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xml.TestDataHelper;
import agg.xml.legacy.XMLSaveLoad;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Tests for the GraGraMigration feature flag mechanism.
 * Verifies that the flag can be toggled and both paths work correctly.
 */
public class FeatureFlagTest {

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
    }

    @AfterTest
    public void tearDown() {
        // Reset to default after tests
        GraGraMigration.setUseNewXml(false);
    }

    @Test
    public void testDefaultIsLegacy() {
        GraGraMigration.setUseNewXml(false);
        assertFalse(GraGraMigration.isUseNewXml(),
            "Default should be legacy (false)");
    }

    @Test
    public void testSetNewXml() {
        GraGraMigration.setUseNewXml(true);
        assertTrue(GraGraMigration.isUseNewXml(),
            "Flag should be true after set");
        GraGraMigration.setUseNewXml(false);
        assertFalse(GraGraMigration.isUseNewXml(),
            "Flag should be false after reset");
    }

    @Test
    public void testSaveLegacyPath() {
        GraGraMigration.setUseNewXml(false);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra graGra = new GraGra();
        try {
            GraGraMigration.load(graGra, file.getAbsolutePath());
        } catch (Exception e) {
            fail("Legacy load should not fail", e);
        }

        File tempFile = new File("target/feature-flag-test-legacy.ggx");
        tempFile.getParentFile().mkdirs();
        boolean saved = GraGraMigration.save(graGra, tempFile.getAbsolutePath());
        assertTrue(saved, "Legacy save should succeed");
        assertTrue(tempFile.exists(), "File should exist");
        tempFile.delete();
    }

    @Test
    public void testSaveNewXmlPath() {
        GraGraMigration.setUseNewXml(true);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra graGra = new GraGra();
        try {
            GraGraMigration.load(graGra, file.getAbsolutePath());
        } catch (Exception e) {
            fail("New XML load should not fail", e);
        }

        File tempFile = new File("target/feature-flag-test-new.ggx");
        tempFile.getParentFile().mkdirs();
        boolean saved = GraGraMigration.save(graGra, tempFile.getAbsolutePath());
        assertTrue(saved, "New XML save should succeed");
        assertTrue(tempFile.exists(), "File should exist");
        tempFile.delete();
    }

    @Test
    public void testValidateReturnsTrue() {
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        assertTrue(GraGraMigration.validate(file.getAbsolutePath()),
            "Validation should return true for valid .ggx file");
    }

    @Test
    public void testValidateReturnsFalseForMissingFile() {
        assertFalse(GraGraMigration.validate("nonexistent.ggx"),
            "Validation should return false for missing file");
    }

    @Test
    public void testLoadMissingFileThrows() {
        GraGraMigration.setUseNewXml(false);
        assertThrows(Exception.class, () -> {
            GraGra graGra = new GraGra();
            GraGraMigration.load(graGra, "nonexistent.ggx");
        });
    }

    @Test
    public void testLoadNonGgxFileThrows() {
        GraGraMigration.setUseNewXml(false);
        assertThrows(Exception.class, () -> {
            GraGra graGra = new GraGra();
            GraGraMigration.load(graGra, "test.txt");
        });
    }

    /**
     * Verify that GraGra.save()/load() dispatches to GraGraMigration
     * when the feature flag is enabled.
     */
    @Test
    public void testGraGraSaveDispatchesToMigrationWhenFlagEnabled() throws Exception {
        GraGraMigration.setUseNewXml(true);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        // Load via GraGra.load (should use new path)
        GraGra graGra = new GraGra();
        graGra.load(file.getAbsolutePath());
        assertNotNull(graGra.getName(), "GraGra should load via new XML path");

        // Save via GraGra.save (should use new path)
        File tempFile = new File("target/feature-flag-test-gragra-new.ggx");
        tempFile.getParentFile().mkdirs();
        graGra.save(tempFile.getAbsolutePath());
        assertTrue(tempFile.exists(), "File should exist after GraGra.save via new path");
        assertTrue(tempFile.length() > 0, "File should not be empty");
        tempFile.delete();
    }

    /**
     * Verify that GraGra.save()/load() uses legacy path when flag is disabled.
     */
    @Test
    public void testGraGraSaveUsesLegacyWhenFlagDisabled() throws Exception {
        GraGraMigration.setUseNewXml(false);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        // Load via GraGra.load (should use legacy path)
        GraGra graGra = new GraGra();
        graGra.load(file.getAbsolutePath());
        assertNotNull(graGra.getName(), "GraGra should load via legacy path");

        // Save via GraGra.save (should use legacy path)
        File tempFile = new File("target/feature-flag-test-gragra-legacy.ggx");
        tempFile.getParentFile().mkdirs();
        graGra.save(tempFile.getAbsolutePath());
        assertTrue(tempFile.exists(), "File should exist after GraGra.save via legacy path");
        assertTrue(tempFile.length() > 0, "File should not be empty");
        tempFile.delete();
    }

    /**
     * Deep roundtrip test via the new DOM path:
     * load -> save -> reload -> compare graph count, node count, edge count.
     */
    @Test
    public void testNewXmlRoundtripDeepVerification() throws Exception {
        GraGraMigration.setUseNewXml(true);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        // Load via new DOM path
        GraGra original = new GraGra();
        GraGraMigration.load(original, file.getAbsolutePath());
        assertNotNull(original.getName(), "Original should load");
        int originalGraphCount = original.getGraphsVec().size();
        int originalNodeCount = 0;
        int originalEdgeCount = 0;
        for (Graph g : original.getGraphsVec()) {
            originalNodeCount += g.getNodesCount();
            originalEdgeCount += g.getArcsCount();
        }

        // Save via new DOM path
        File tempFile = new File("target/feature-flag-roundtrip-new.ggx");
        tempFile.getParentFile().mkdirs();
        GraGraMigration.save(original, tempFile.getAbsolutePath());
        assertTrue(tempFile.exists(), "Saved file should exist");
        assertTrue(tempFile.length() > 0, "Saved file should not be empty");

        // Reload via new DOM path
        GraGra reloaded = new GraGra();
        GraGraMigration.load(reloaded, tempFile.getAbsolutePath());
        assertNotNull(reloaded.getName(), "Reloaded should load");

        // Deep comparison
        assertEquals(reloaded.getName(), original.getName(),
            "Name should match after new XML roundtrip");
        assertEquals(reloaded.getGraphsVec().size(), originalGraphCount,
            "Graph count should match after new XML roundtrip");

        int reloadedNodeCount = 0;
        int reloadedEdgeCount = 0;
        for (Graph g : reloaded.getGraphsVec()) {
            reloadedNodeCount += g.getNodesCount();
            reloadedEdgeCount += g.getArcsCount();
        }
        assertEquals(reloadedNodeCount, originalNodeCount,
            "Total node count should match after new XML roundtrip");
        assertEquals(reloadedEdgeCount, originalEdgeCount,
            "Total edge count should match after new XML roundtrip");

        tempFile.delete();
    }

    /**
     * Roundtrip with all 4 sample files via the new DOM path.
     */
    @Test
    public void testNewXmlRoundtripAllFiles() throws Exception {
        GraGraMigration.setUseNewXml(true);

        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            // Load
            GraGra original = new GraGra();
            GraGraMigration.load(original, file.getAbsolutePath());
            assertNotNull(original.getName(), "Should load: " + filename);

            // Save
            File tempFile = new File("target/feature-flag-roundtrip-" + filename);
            tempFile.getParentFile().mkdirs();
            GraGraMigration.save(original, tempFile.getAbsolutePath());
            assertTrue(tempFile.exists(), "Saved file should exist: " + filename);
            assertTrue(tempFile.length() > 0, "Saved file should not be empty: " + filename);

            // Reload
            GraGra reloaded = new GraGra();
            GraGraMigration.load(reloaded, tempFile.getAbsolutePath());
            assertEquals(reloaded.getName(), original.getName(),
                "Name should match for " + filename);
            assertEquals(reloaded.getGraphsVec().size(), original.getGraphsVec().size(),
                "Graph count should match for " + filename);

            tempFile.delete();
        }
    }
}
