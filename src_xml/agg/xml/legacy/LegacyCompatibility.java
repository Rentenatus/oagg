/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.legacy;

import agg.util.XMLHelper;
import agg.util.XMLObject;
import agg.xml.core.XMLSerializationException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Legacy compatibility layer providing static methods for serializing and
 * deserializing {@link XMLObject} instances using the existing {@link XMLHelper}.
 *
 * <p>This class serves as a bridge during the migration from the legacy
 * XMLObject/XMLHelper system to the new XMLSerializable system. It allows
 * existing code to continue working while the new infrastructure is being
 * rolled out.</p>
 */
public final class LegacyCompatibility {

    private LegacyCompatibility() {
    }

    /**
     * Serializes an XMLObject to a file using the legacy XMLHelper.
     *
     * @param xmlObject The XMLObject to serialize
     * @param file      The output file
     * @throws XMLSerializationException if serialization fails
     */
    public static void serializeToFile(XMLObject xmlObject, File file) throws XMLSerializationException {
        if (xmlObject == null) {
            throw new IllegalArgumentException("XMLObject cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(xmlObject);
        if (!helper.save_to_xml(file.getAbsolutePath())) {
            throw new XMLSerializationException("Failed to serialize to " + file.getAbsolutePath());
        }
    }

    /**
     * Deserializes an XMLObject from a file using the legacy XMLHelper.
     *
     * @param <T>         The type of the XMLObject
     * @param file        The XML file
     * @param type        The target class (must have a no-arg constructor and implement XMLObject)
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public static <T extends XMLObject> T deserializeFromFile(File file, Class<T> type)
            throws XMLSerializationException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            XMLHelper helper = new XMLHelper();
            if (!helper.read_from_xml(file.getAbsolutePath())) {
                throw new XMLSerializationException("Failed to read from " + file.getAbsolutePath());
            }
            helper.getTopObject(instance);
            return instance;
        } catch (XMLSerializationException e) {
            throw e;
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new XMLSerializationException(
                "Failed to create instance of " + type.getName(), e);
        }
    }

    /**
     * Deserializes an XMLObject from a file into an existing instance.
     *
     * @param file     The XML file
     * @param template The template object to populate
     * @throws XMLSerializationException if deserialization fails
     */
    public static void deserializeFromFile(File file, XMLObject template)
            throws XMLSerializationException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        XMLHelper helper = new XMLHelper();
        if (!helper.read_from_xml(file.getAbsolutePath())) {
            throw new XMLSerializationException("Failed to read from " + file.getAbsolutePath());
        }
        helper.getTopObject(template);
    }

    /**
     * Serializes an XMLObject to an XML string using a temporary file.
     *
     * @param xmlObject The XMLObject to serialize
     * @return The XML string representation
     * @throws XMLSerializationException if serialization fails
     */
    public static String serializeToString(XMLObject xmlObject) throws XMLSerializationException {
        if (xmlObject == null) {
            throw new IllegalArgumentException("XMLObject cannot be null");
        }
        File tempFile = null;
        try {
            tempFile = File.createTempFile("agg-xml-", ".ggx");
            tempFile.deleteOnExit();
            serializeToFile(xmlObject, tempFile);
            return new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), "UTF-8");
        } catch (XMLSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to serialize to string", e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    /**
     * Checks whether a file is a valid AGG XML file that can be loaded by the legacy XMLHelper.
     *
     * @param file The file to check
     * @return true if the file can be loaded, false otherwise
     */
    public static boolean canLoad(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return false;
        }
        XMLHelper helper = new XMLHelper();
        return helper.read_from_xml(file.getAbsolutePath());
    }
}
