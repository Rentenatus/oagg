/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import agg.xml.mapper.TypeRegistry;

/**
 * Factory class for creating {@link XMLDeserializer} instances.
 *
 * <p>This factory provides static methods to create pre-configured
 * {@link DOMXMLDeserializer} instances, optionally with a type registry
 * for polymorphic deserialization.</p>
 */
public final class XMLDeserializerFactory {

    private XMLDeserializerFactory() {
    }

    /**
     * Creates a default deserializer with no type registry.
     *
     * @return A new XMLDeserializer instance
     */
    public static XMLDeserializer createDeserializer() {
        return new DOMXMLDeserializer();
    }

    /**
     * Creates a deserializer with the specified type registry.
     *
     * @param typeRegistry The type registry for polymorphic deserialization
     * @return A new XMLDeserializer instance
     */
    public static XMLDeserializer createDeserializer(TypeRegistry typeRegistry) {
        return new DOMXMLDeserializer(typeRegistry);
    }

    /**
     * Creates a deserializer with pre-registered AGG domain types.
     *
     * @return A new XMLDeserializer instance with AGG types registered
     */
    public static XMLDeserializer createDeserializerWithAggTypes() {
        return new DOMXMLDeserializer(new TypeRegistry(true));
    }
}
