/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xml.TestDataHelper;
import agg.xml.core.XMLSerializationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Unit tests for {@link XMLValidator}.
 */
public class XMLValidatorTest {

    private XMLValidator validator;

    @BeforeClass
    public void setUp() throws XMLSerializationException {
        TestDataHelper.requireAllSamples();
        validator = new XMLValidator();
    }

    @Test
    public void testValidatorCreation() {
        assertNotNull(validator, "Validator should be created");
    }

    @Test
    public void testValidXmlString() throws XMLSerializationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document version=\"1.0\">\n" +
                     "  <GraphTransformationSystem ID=\"I0\" directed=\"true\"\n" +
                     "      name=\"Test\" parallel=\"true\">\n" +
                     "    <Types/>\n" +
                     "  </GraphTransformationSystem>\n" +
                     "</Document>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        boolean valid = validator.validate(stream);
        assertTrue(valid, "Valid XML should pass validation");
        assertFalse(validator.hasErrors(), "Should have no errors");
    }

    @Test
    public void testInvalidRoot() throws XMLSerializationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<WrongRoot version=\"1.0\">\n" +
                     "  <GraphTransformationSystem/>\n" +
                     "</WrongRoot>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        boolean valid = validator.validate(stream);
        assertFalse(valid, "Invalid root element should fail validation");
        assertTrue(validator.hasErrors(), "Should have errors");
    }

    @Test
    public void testMissingVersion() throws XMLSerializationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document>\n" +
                     "  <GraphTransformationSystem/>\n" +
                     "</Document>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        validator.validate(stream);
        assertTrue(validator.hasErrors(), "Missing version should produce errors");
    }

    @Test
    public void testValidateFile() throws XMLSerializationException {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        boolean valid = validator.validate(ggxFile);
        assertTrue(valid, "Sample .ggx file should be valid: " + validator.getIssues());
    }

    @Test
    public void testValidateAllSampleFiles() throws XMLSerializationException {
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File file = TestDataHelper.resolveSample(filename);
            boolean valid = validator.validate(file);
            assertTrue(valid, filename + " should be valid. Issues: " + validator.getIssues());
        }
    }

    @Test
    public void testGetIssuesAfterValidation() throws XMLSerializationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<WrongRoot version=\"1.0\"/>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        validator.validate(stream);
        assertFalse(validator.getIssues().isEmpty(), "Should have issues for invalid XML");
    }

    @Test
    public void testCustomSchemaFile() throws XMLSerializationException {
        File schemaFile = new File("../src_xml/agg/xml/validation/agg-ggx-schema.xsd");
        if (schemaFile.exists()) {
            XMLValidator customValidator = new XMLValidator(schemaFile);
            assertNotNull(customValidator, "Should create validator from schema file");
        }
    }

    @Test
    public void testDocumentWithoutGraphTransformationSystemFails() throws XMLSerializationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document version=\"1.0\">\n" +
                     "  <WrongElement/>\n" +
                     "</Document>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        boolean valid = validator.validate(stream);
        assertFalse(valid, "Document without GraphTransformationSystem should fail validation");
    }

    @Test
    public void testFatalErrorCausesValidationFailure() throws XMLSerializationException {
        // Unclosed tag triggers a fatal parse error
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document version=\"1.0\">\n" +
                     "  <GraphTransformationSystem>\n" +
                     "</Document>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        boolean valid = validator.validate(stream);
        assertFalse(valid, "Malformed XML with unclosed tag should fail validation");
    }
}
