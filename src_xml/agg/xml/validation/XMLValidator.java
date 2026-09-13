/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * XML Schema validation framework for .ggx files and other XML documents.
 *
 * <p>This class provides methods to validate XML documents against XML Schema
 * definitions (XSD). It collects all validation errors and warnings, allowing
 * callers to inspect the full list of issues rather than failing on the first
 * error.</p>
 *
 * <p>The default schema for AGG .ggx files is bundled as
 * {@code agg-ggx-schema.xsd} in the same package.</p>
 */
public class XMLValidator {

    private final Schema schema;
    private final List<ValidationIssue> issues;

    /**
     * Creates a validator using the bundled AGG .ggx schema.
     *
     * @throws XMLSerializationException if the schema cannot be loaded
     */
    public XMLValidator() throws XMLSerializationException {
        this(loadDefaultSchema());
    }

    /**
     * Creates a validator using the specified schema file.
     *
     * @param schemaFile The XSD schema file
     * @throws XMLSerializationException if the schema cannot be loaded
     */
    public XMLValidator(File schemaFile) throws XMLSerializationException {
        this(loadSchemaFromFile(schemaFile));
    }

    /**
     * Creates a validator using the specified schema input stream.
     *
     * @param schemaStream The XSD schema input stream
     * @throws XMLSerializationException if the schema cannot be loaded
     */
    public XMLValidator(InputStream schemaStream) throws XMLSerializationException {
        this(loadSchemaFromStream(schemaStream));
    }

    private XMLValidator(Schema schema) {
        this.schema = schema;
        this.issues = new ArrayList<>();
    }

    /**
     * Validates an XML file against the schema.
     *
     * @param xmlFile The XML file to validate
     * @return true if valid, false if validation errors were found
     * @throws XMLSerializationException if validation cannot be performed
     */
    public boolean validate(File xmlFile) throws XMLSerializationException {
        issues.clear();
        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new CollectingErrorHandler(issues));
            validator.validate(new StreamSource(xmlFile));
            return issues.stream().noneMatch(i -> i.getSeverity() == Severity.ERROR);
        } catch (SAXException e) {
            issues.add(new ValidationIssue(Severity.ERROR, e.getMessage(), -1, -1));
            return false;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to validate file: " + xmlFile, e);
        }
    }

    /**
     * Validates an XML DOM document against the schema.
     *
     * @param document The DOM document to validate
     * @return true if valid, false if validation errors were found
     * @throws XMLSerializationException if validation cannot be performed
     */
    public boolean validate(Document document) throws XMLSerializationException {
        issues.clear();
        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new CollectingErrorHandler(issues));
            validator.validate(new DOMSource(document));
            return issues.stream().noneMatch(i -> i.getSeverity() == Severity.ERROR);
        } catch (SAXException e) {
            issues.add(new ValidationIssue(Severity.ERROR, e.getMessage(), -1, -1));
            return false;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to validate document", e);
        }
    }

    /**
     * Validates an XML input stream against the schema.
     *
     * @param xmlStream The XML input stream to validate
     * @return true if valid, false if validation errors were found
     * @throws XMLSerializationException if validation cannot be performed
     */
    public boolean validate(InputStream xmlStream) throws XMLSerializationException {
        issues.clear();
        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new CollectingErrorHandler(issues));
            validator.validate(new StreamSource(xmlStream));
            return issues.stream().noneMatch(i -> i.getSeverity() == Severity.ERROR);
        } catch (SAXException e) {
            issues.add(new ValidationIssue(Severity.ERROR, e.getMessage(), -1, -1));
            return false;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to validate stream", e);
        }
    }

    /**
     * Returns the validation issues from the most recent validation call.
     *
     * @return An unmodifiable list of validation issues
     */
    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    /**
     * Returns true if the most recent validation produced errors.
     *
     * @return true if there are error-severity issues
     */
    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.getSeverity() == Severity.ERROR);
    }

    /**
     * Returns true if the most recent validation produced warnings.
     *
     * @return true if there are warning-severity issues
     */
    public boolean hasWarnings() {
        return issues.stream().anyMatch(i -> i.getSeverity() == Severity.WARNING);
    }

    private static Schema loadDefaultSchema() throws XMLSerializationException {
        InputStream stream = XMLValidator.class.getResourceAsStream("agg-ggx-schema.xsd");
        if (stream == null) {
            throw new XMLSerializationException("Default AGG schema not found on classpath");
        }
        return loadSchemaFromStream(stream);
    }

    private static Schema loadSchemaFromFile(File schemaFile) throws XMLSerializationException {
        if (schemaFile == null || !schemaFile.exists()) {
            throw new XMLSerializationException("Schema file not found: " + schemaFile);
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return factory.newSchema(schemaFile);
        } catch (SAXException e) {
            throw new XMLSerializationException("Failed to load schema: " + schemaFile, e);
        }
    }

    private static Schema loadSchemaFromStream(InputStream schemaStream) throws XMLSerializationException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return factory.newSchema(new StreamSource(schemaStream));
        } catch (SAXException e) {
            throw new XMLSerializationException("Failed to load schema from stream", e);
        }
    }

    /**
     * Error handler that collects all validation issues.
     */
    private static class CollectingErrorHandler implements ErrorHandler {

        private final List<ValidationIssue> issues;

        CollectingErrorHandler(List<ValidationIssue> issues) {
            this.issues = issues;
        }

        @Override
        public void warning(SAXParseException e) {
            issues.add(new ValidationIssue(Severity.WARNING, e.getMessage(),
                e.getLineNumber(), e.getColumnNumber()));
        }

        @Override
        public void error(SAXParseException e) {
            issues.add(new ValidationIssue(Severity.ERROR, e.getMessage(),
                e.getLineNumber(), e.getColumnNumber()));
        }

        @Override
        public void fatalError(SAXParseException e) {
            issues.add(new ValidationIssue(Severity.FATAL, e.getMessage(),
                e.getLineNumber(), e.getColumnNumber()));
        }
    }

    /**
     * Severity levels for validation issues.
     */
    public enum Severity {
        WARNING, ERROR, FATAL
    }

    /**
     * Represents a single validation issue.
     */
    public static class ValidationIssue {

        private final Severity severity;
        private final String message;
        private final int lineNumber;
        private final int columnNumber;

        ValidationIssue(Severity severity, String message, int lineNumber, int columnNumber) {
            this.severity = severity;
            this.message = message;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        @Override
        public String toString() {
            return String.format("[%s] line %d, col %d: %s",
                severity, lineNumber, columnNumber, message);
        }
    }
}
