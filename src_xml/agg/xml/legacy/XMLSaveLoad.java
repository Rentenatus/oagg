/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.legacy;

import agg.util.XMLObject;
import agg.xml.core.DOMXMLDeserializer;
import agg.xml.core.DOMXMLSerializer;
import agg.xml.core.XMLSerializationException;
import agg.xml.mapper.TypeRegistry;

import java.io.File;

/**
 * High-level Save/Load API providing simple static methods for serializing
 * and deserializing objects to and from XML files.
 *
 * <p>This class abstracts away the details of choosing between the new
 * XMLSerializable infrastructure and the legacy XMLObject/XMLHelper system.
 * For {@link XMLObject} instances, it delegates to {@link LegacyCompatibility};
 * for {@link agg.xml.core.XMLSerializable} instances, it uses the new
 * DOM-based serializer/deserializer.</p>
 */
public final class XMLSaveLoad {

    private XMLSaveLoad() {
    }

    /**
     * Saves an object to a file.
     *
     * @param object The object to save
     * @param file   The output file
     * @throws XMLSerializationException if serialization fails
     */
    public static void saveObject(Object object, File file) throws XMLSerializationException {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (object instanceof XMLObject) {
            LegacyCompatibility.serializeToFile((XMLObject) object, file);
        } else {
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(object, file);
        }
    }

    /**
     * Saves an object to a file with the specified encoding and indentation settings.
     *
     * @param object  The object to save
     * @param file    The output file
     * @param encoding The character encoding
     * @param indent  Whether to indent the output
     * @throws XMLSerializationException if serialization fails
     */
    public static void saveObject(Object object, File file, String encoding, boolean indent)
            throws XMLSerializationException {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (object instanceof XMLObject) {
            LegacyCompatibility.serializeToFile((XMLObject) object, file);
        } else {
            DOMXMLSerializer serializer = new DOMXMLSerializer(encoding, indent, "  ");
            serializer.serializeToFile(object, file);
        }
    }

    /**
     * Loads an object from a file using the legacy XMLHelper.
     *
     * @param <T>  The expected type
     * @param file The XML file
     * @param type The target class (must implement XMLObject)
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public static <T extends XMLObject> T loadObject(File file, Class<T> type)
            throws XMLSerializationException {
        return LegacyCompatibility.deserializeFromFile(file, type);
    }

    /**
     * Loads an XMLSerializable object from a file using the new DOM-based deserializer.
     *
     * @param <T>  The expected type
     * @param file The XML file
     * @param type The target class (must implement XMLSerializable)
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public static <T> T loadSerializable(File file, Class<T> type)
            throws XMLSerializationException {
        DOMXMLDeserializer deserializer = new DOMXMLDeserializer();
        return deserializer.deserializeFromFile(file, type);
    }

    /**
     * Loads an XMLSerializable object from a file using the new DOM-based deserializer
     * with the specified type registry.
     *
     * @param <T>          The expected type
     * @param file         The XML file
     * @param type         The target class
     * @param typeRegistry The type registry for polymorphic deserialization
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public static <T> T loadSerializable(File file, Class<T> type, TypeRegistry typeRegistry)
            throws XMLSerializationException {
        DOMXMLDeserializer deserializer = new DOMXMLDeserializer(typeRegistry);
        return deserializer.deserializeFromFile(file, type);
    }

    /**
     * Serializes an object to an XML string.
     *
     * @param object The object to serialize
     * @return The XML string representation
     * @throws XMLSerializationException if serialization fails
     */
    public static String saveToString(Object object) throws XMLSerializationException {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (object instanceof XMLObject) {
            return LegacyCompatibility.serializeToString((XMLObject) object);
        }
        DOMXMLSerializer serializer = new DOMXMLSerializer();
        return serializer.serializeToString(object);
    }
}
