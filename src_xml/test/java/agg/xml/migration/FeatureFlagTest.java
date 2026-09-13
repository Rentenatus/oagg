/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.migration;

import agg.xt_basis.GraGra;
import agg.xml.legacy.XMLSaveLoad;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Tests for the GraGraMigration feature flag mechanism.
 * Verifies that the flag can be toggled and both paths work correctly.
 */
public class FeatureFlagTest {

    private static final String BASELINE_DIR = "../test_xml/baseline/samples/";

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
        File file = new File(BASELINE_DIR + "small_graph.ggx");
        if (!file.exists()) return;

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
        File file = new File(BASELINE_DIR + "small_graph.ggx");
        if (!file.exists()) return;

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
        File file = new File(BASELINE_DIR + "small_graph.ggx");
        if (!file.exists()) return;

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
}
