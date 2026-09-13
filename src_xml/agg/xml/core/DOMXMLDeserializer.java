/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import agg.xml.mapper.TypeRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;

/**
 * DOM-based implementation of {@link XMLDeserializer}.
 *
 * <p>This deserializer parses an XML file or stream into a DOM document,
 * creates a {@link DOMXMLDeserializerContext} from the root element, and
 * delegates to the target object's {@link XMLSerializable#deserialize} method.</p>
 */
public class DOMXMLDeserializer implements XMLDeserializer {

    private final TypeRegistry typeRegistry;

    /**
     * Creates a new deserializer with no type registry.
     */
    public DOMXMLDeserializer() {
        this(null);
    }

    /**
     * Creates a new deserializer with the specified type registry.
     *
     * @param typeRegistry The type registry for polymorphic deserialization
     */
    public DOMXMLDeserializer(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Class<T> targetClass, XMLDeserializerContext context) throws XMLSerializationException {
        try {
            T object = targetClass.getDeclaredConstructor().newInstance();
            if (object instanceof XMLSerializable) {
                ((XMLSerializable) object).deserialize(context);
            }
            return object;
        } catch (XMLSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLSerializationException(
                "Failed to deserialize object of type " + targetClass.getName(), e);
        }
    }

    @Override
    public Object beginDeserialize(XMLDeserializerContext context) throws XMLSerializationException {
        String typeName = context.getCurrentElementName();
        if (typeRegistry != null && typeRegistry.isTypeRegistered(typeName)) {
            return typeRegistry.createInstance(typeName);
        }
        return null;
    }

    @Override
    public void endDeserialize(Object object, XMLDeserializerContext context) throws XMLSerializationException {
        if (object instanceof XMLSerializable) {
            ((XMLSerializable) object).deserialize(context);
        }
    }

    /**
     * Deserializes an object from an XML file.
     *
     * @param <T>         The expected type
     * @param file        The XML file
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if parsing or deserialization fails
     */
    public <T> T deserializeFromFile(File file, Class<T> targetClass) throws XMLSerializationException {
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
        return deserialize(targetClass, context);
    }

    /**
     * Deserializes an object from an XML input stream.
     *
     * @param <T>         The expected type
     * @param inputStream The XML input stream
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if parsing or deserialization fails
     */
    public <T> T deserializeFromStream(InputStream inputStream, Class<T> targetClass) throws XMLSerializationException {
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(inputStream);
        return deserialize(targetClass, context);
    }

    /**
     * Deserializes an object from a DOM document.
     *
     * @param <T>         The expected type
     * @param document    The DOM document
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public <T> T deserializeFromDocument(Document document, Class<T> targetClass) throws XMLSerializationException {
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(document);
        return deserialize(targetClass, context);
    }

    /**
     * Deserializes an object from a DOM element.
     *
     * @param <T>         The expected type
     * @param element     The DOM element
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    public <T> T deserializeFromElement(Element element, Class<T> targetClass) throws XMLSerializationException {
        Document doc = element.getOwnerDocument();
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(doc);
        return deserialize(targetClass, context);
    }

    /**
     * Deserializes an object from an XML string.
     *
     * @param <T>         The expected type
     * @param xmlString   The XML string
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if parsing or deserialization fails
     */
    public <T> T deserializeFromString(String xmlString, Class<T> targetClass) throws XMLSerializationException {
        try {
            java.io.ByteArrayInputStream inputStream =
                new java.io.ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            return deserializeFromStream(inputStream, targetClass);
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to deserialize from string", e);
        }
    }

    /**
     * Gets the type registry used by this deserializer.
     *
     * @return The type registry, or null if none
     */
    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
