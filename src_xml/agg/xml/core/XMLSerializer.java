/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

/**
 * Interface for XML serializers that convert objects to XML representation.
 * This interface provides the abstraction for writing objects to XML output.
 */
public interface XMLSerializer {
    
    /**
     * Serializes an object to XML.
     * 
     * @param object The object to serialize
     * @param context The serializer context
     * @throws XMLSerializationException if serialization fails
     */
    void serialize(Object object, XMLSerializerContext context) throws XMLSerializationException;
    
    /**
     * Begins serialization of a complex object.
     * 
     * @param object The object to serialize
     * @param context The serializer context
     * @return true if the object was handled, false otherwise
     * @throws XMLSerializationException if serialization fails
     */
    boolean beginSerialize(Object object, XMLSerializerContext context) throws XMLSerializationException;
    
    /**
     * Ends serialization of a complex object.
     * 
     * @param object The object being serialized
     * @param context The serializer context
     * @throws XMLSerializationException if serialization fails
     */
    void endSerialize(Object object, XMLSerializerContext context) throws XMLSerializationException;
}
