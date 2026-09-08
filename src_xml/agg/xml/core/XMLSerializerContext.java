/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.w3c.dom.Element;

/**
 * Context interface providing services for XML serialization.
 * Implementations of this interface manage the XML document being built
 * and provide helper methods for creating elements and attributes.
 */
public interface XMLSerializerContext {
    
    /**
     * Gets the current element being serialized.
     * 
     * @return The current DOM element
     */
    Element getCurrentElement();
    
    /**
     * Creates a new child element with the specified name.
     * 
     * @param name The element name
     * @return The newly created element
     */
    Element createElement(String name);
    
    /**
     * Creates a new child element with the specified name and adds it to the current element.
     * 
     * @param name The element name
     * @return The newly created and appended element
     */
    Element createAndAppendElement(String name);
    
    /**
     * Sets an attribute on the current element.
     * 
     * @param name The attribute name
     * @param value The attribute value
     */
    void setAttribute(String name, String value);
    
    /**
     * Sets a boolean attribute on the current element.
     * 
     * @param name The attribute name
     * @param value The boolean value
     */
    void setAttribute(String name, boolean value);
    
    /**
     * Sets an integer attribute on the current element.
     * 
     * @param name The attribute name
     * @param value The integer value
     */
    void setAttribute(String name, int value);
    
    /**
     * Sets a double attribute on the current element.
     * 
     * @param name The attribute name
     * @param value The double value
     */
    void setAttribute(String name, double value);
    
    /**
     * Sets text content for the current element.
     * 
     * @param text The text content
     */
    void setTextContent(String text);
    
    /**
     * Serializes an object using the appropriate serializer.
     * 
     * @param object The object to serialize
     * @throws XMLSerializationException if serialization fails
     */
    void serializeObject(Object object) throws XMLSerializationException;
    
    /**
     * Pushes a new element context onto the stack.
     * 
     * @param element The element to push
     */
    void pushElement(Element element);
    
    /**
     * Pops the current element context from the stack.
     * 
     * @return The popped element
     */
    Element popElement();
    
    /**
     * Gets the root document element.
     * 
     * @return The root element
     */
    Element getRootElement();
    
    /**
     * Returns the XML as a string.
     * 
     * @return The XML string representation
     * @throws XMLSerializationException if serialization fails
     */
    String toXMLString() throws XMLSerializationException;
}
