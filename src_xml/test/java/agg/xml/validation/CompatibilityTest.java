/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.legacy.LegacyCompatibility;
import agg.xml.legacy.XMLSaveLoad;
import agg.xml.migration.GraGraMigration;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Compatibility tests verifying that all existing .ggx files can be loaded
 * by both the legacy XMLHelper and the new DOM-based system.
 *
 * <p>These tests ensure 100% backward compatibility as required by the
 * refactoring plan's success criteria.</p>
 */
public class CompatibilityTest {

    private static final String BASELINE_DIR = "../test_xml/baseline/samples/";

    private static final String[] SAMPLE_FILES = {
        "small_graph.ggx",
        "small_graph_layered.ggx",
        "medium_graph.ggx",
        "large_graph.ggx"
    };

    @Test
    public void testAllFilesLoadWithLegacyHelper() {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            XMLHelper helper = new XMLHelper();
            boolean loaded = helper.read_from_xml(file.getAbsolutePath());
            assertTrue(loaded, "Legacy should load: " + filename);
            assertNotNull(helper.getDoc(), "Document should exist: " + filename);
            assertEquals(helper.getDoc().getDocumentElement().getNodeName(), "Document",
                "Root should be Document: " + filename);
        }
    }

    @Test
    public void testAllFilesLoadWithDomContext() throws Exception {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            assertNotNull(context.getDocument(), "DOM document should exist: " + filename);
            assertEquals(context.getCurrentElement().getNodeName(), "Document",
                "Root should be Document: " + filename);
        }
    }

    @Test
    public void testAllFilesLoadWithGraGra() throws Exception {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            GraGra graGra = new GraGra();
            graGra.load(file.getAbsolutePath());
            assertNotNull(graGra.getName(), "Name should not be null: " + filename);
        }
    }

    @Test
    public void testAllFilesValidWithSchema() throws Exception {
        XMLValidator validator = new XMLValidator();
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            assertTrue(validator.validate(file),
                filename + " should be schema-valid. Issues: " + validator.getIssues());
        }
    }

    @Test
    public void testLegacyCompatibilityCanLoad() {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            assertTrue(LegacyCompatibility.canLoad(file),
                "LegacyCompatibility.canLoad should return true: " + filename);
        }
    }

    @Test
    public void testGraGraMigrationLegacyPath() throws Exception {
        GraGraMigration.setUseNewXml(false);
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            GraGra graGra = new GraGra();
            boolean loaded = GraGraMigration.load(graGra, file.getAbsolutePath());
            assertTrue(loaded, "GraGraMigration.load should succeed (legacy): " + filename);
        }
    }

    @Test
    public void testGraGraMigrationValidation() {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            assertTrue(GraGraMigration.validate(file.getAbsolutePath()),
                "GraGraMigration.validate should return true: " + filename);
        }
    }

    @Test
    public void testVersionAttributePreserved() throws Exception {
        for (String filename : SAMPLE_FILES) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            XMLHelper helper = new XMLHelper();
            helper.read_from_xml(file.getAbsolutePath());
            String legacyVersion = helper.getDoc().getDocumentElement().getAttribute("version");

            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            String newVersion = context.getCurrentElement().getAttribute("version");

            assertEquals(newVersion, legacyVersion,
                "Version should match between systems: " + filename);
            assertFalse(legacyVersion.isEmpty(), "Version should not be empty: " + filename);
        }
    }
}
