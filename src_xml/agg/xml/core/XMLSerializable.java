/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

/**
 * Marker interface for objects that can be serialized to and deserialized from XML.
 * This is the new interface that will replace the XMLObject interface in the refactored
 * architecture.
 * 
 * <p>This interface follows the Adapter Pattern design, allowing legacy XMLObject
 * implementations to coexist with new XMLSerializable implementations through
 * adapter classes.</p>
 * 
 * <p>Key design principles:</p>
 * <ul>
 *   <li>Separation of concerns: Domain objects should not know about XML serialization</li>
 *   <li>Dependency inversion: Domain modules depend on abstractions, not concretions</li>
 *   <li>Zero changes to domain: Existing Graph, Rule, Node, Arc classes remain unchanged</li>
 * </ul>
 */
public interface XMLSerializable {
    
    /**
     * Serializes this object to XML using the provided serializer context.
     * 
     * @param context The serializer context providing serialization services
     * @throws XMLSerializationException if serialization fails
     */
    void serialize(XMLSerializerContext context) throws XMLSerializationException;
    
    /**
     * Deserializes this object from XML using the provided deserializer context.
     * 
     * @param context The deserializer context providing deserialization services
     * @throws XMLSerializationException if deserialization fails
     */
    void deserialize(XMLDeserializerContext context) throws XMLSerializationException;
}
