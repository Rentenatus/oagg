/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xt_basis.GraGra;
import agg.xml.TestDataHelper;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.migration.GraGraMigration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Compatibility tests verifying that all existing .ggx files can be loaded
 * by the DOM-based system and the schema.
 */
public class CompatibilityTest {

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
    }



    @Test
    public void testAllFilesLoadWithDomContext() throws Exception {
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            assertNotNull(context.getDocument(), "DOM document should exist: " + filename);
            assertEquals(context.getCurrentElement().getNodeName(), "Document",
                "Root should be Document: " + filename);
        }
    }

    @Test
    public void testAllFilesLoadWithGraGra() throws Exception {
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            GraGra graGra = new GraGra();
            graGra.load(file.getAbsolutePath());
            assertNotNull(graGra.getName(), "Name should not be null: " + filename);
        }
    }

    @Test
    public void testAllFilesValidWithSchema() throws Exception {
        XMLValidator validator = new XMLValidator();
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            assertTrue(validator.validate(file),
                filename + " should be schema-valid. Issues: " + validator.getIssues());
        }
    }

    @Test
    public void testGraGraMigrationValidation() {
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            assertTrue(GraGraMigration.validate(file.getAbsolutePath()),
                "GraGraMigration.validate should return true: " + filename);
        }
    }

    @Test
    public void testVersionAttributePreserved() throws Exception {
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);

            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            String version = context.getCurrentElement().getAttribute("version");
            assertFalse(version.isEmpty(), "Version should not be empty: " + filename);
        }
    }
}
