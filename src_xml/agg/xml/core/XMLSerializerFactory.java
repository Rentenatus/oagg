/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

/**
 * Factory class for creating {@link XMLSerializer} instances.
 *
 * <p>This factory provides static methods to create pre-configured
 * {@link DOMXMLSerializer} instances with common settings.</p>
 */
public final class XMLSerializerFactory {

    private XMLSerializerFactory() {
    }

    /**
     * Creates a default serializer with UTF-8 encoding and indentation.
     *
     * @return A new XMLSerializer instance
     */
    public static XMLSerializer createSerializer() {
        return new DOMXMLSerializer();
    }

    /**
     * Creates a serializer with the specified settings.
     *
     * @param encoding     The character encoding
     * @param indent       Whether to indent the output
     * @param indentString The indentation string
     * @return A new XMLSerializer instance
     */
    public static XMLSerializer createSerializer(String encoding, boolean indent, String indentString) {
        return new DOMXMLSerializer(encoding, indent, indentString);
    }

    /**
     * Creates a serializer with indentation disabled (compact output).
     *
     * @return A new XMLSerializer instance with compact output
     */
    public static XMLSerializer createCompactSerializer() {
        return new DOMXMLSerializer("UTF-8", false, "");
    }
}
