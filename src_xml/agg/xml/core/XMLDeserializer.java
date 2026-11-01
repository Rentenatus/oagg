/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

/**
 * Interface for XML deserializers that convert XML representation to objects.
 * This interface provides the abstraction for reading objects from XML input.
 */
public interface XMLDeserializer {
    
    /**
     * Deserializes an object from XML.
     * 
     * @param <T> The type of the object to deserialize
     * @param targetClass The target class
     * @param context The deserializer context
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    <T> T deserialize(Class<T> targetClass, XMLDeserializerContext context) throws XMLSerializationException;
    
    /**
     * Begins deserialization of a complex object.
     * 
     * @param context The deserializer context
     * @return The object being deserialized, or null if not handled
     * @throws XMLSerializationException if deserialization fails
     */
    Object beginDeserialize(XMLDeserializerContext context) throws XMLSerializationException;
    
    /**
     * Ends deserialization of a complex object.
     * 
     * @param object The object being deserialized
     * @param context The deserializer context
     * @throws XMLSerializationException if deserialization fails
     */
    void endDeserialize(Object object, XMLDeserializerContext context) throws XMLSerializationException;
}
