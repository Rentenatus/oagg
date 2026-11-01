/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.w3c.dom.Element;

/**
 * Context interface providing services for XML deserialization.
 * Implementations of this interface parse the XML document and provide
 * helper methods for reading elements and attributes.
 */
public interface XMLDeserializerContext {
    
    /**
     * Gets the current element being deserialized.
     * 
     * @return The current DOM element
     */
    Element getCurrentElement();
    
    /**
     * Gets the name of the current element.
     * 
     * @return The element name
     */
    String getCurrentElementName();
    
    /**
     * Gets an attribute value from the current element.
     * 
     * @param name The attribute name
     * @return The attribute value, or null if not present
     */
    String getAttribute(String name);
    
    /**
     * Gets a boolean attribute from the current element.
     * 
     * @param name The attribute name
     * @param defaultValue The default value if attribute is not present
     * @return The boolean value
     */
    boolean getAttribute(String name, boolean defaultValue);
    
    /**
     * Gets an integer attribute from the current element.
     * 
     * @param name The attribute name
     * @param defaultValue The default value if attribute is not present
     * @return The integer value
     */
    int getAttribute(String name, int defaultValue);
    
    /**
     * Gets a double attribute from the current element.
     * 
     * @param name The attribute name
     * @param defaultValue The default value if attribute is not present
     * @return The double value
     */
    double getAttribute(String name, double defaultValue);
    
    /**
     * Gets the text content of the current element.
     * 
     * @return The text content, or null if empty
     */
    String getTextContent();
    
    /**
     * Gets the first child element with the specified name.
     * 
     * @param name The element name
     * @return The child element, or null if not found
     */
    Element getFirstChildElement(String name);
    
    /**
     * Gets all child elements with the specified name.
     * 
     * @param name The element name
     * @return Array of child elements
     */
    Element[] getChildElements(String name);
    
    /**
     * Moves to the first child element.
     * 
     * @return true if moved to a child element, false otherwise
     */
    boolean moveToFirstChild();
    
    /**
     * Moves to the next sibling element.
     * 
     * @return true if moved to a sibling element, false otherwise
     */
    boolean moveToNextSibling();
    
    /**
     * Moves to the parent element.
     * 
     * @return true if moved to parent element, false otherwise
     */
    boolean moveToParent();
    
    /**
     * Deserializes an object using the appropriate deserializer.
     * 
     * @param <T> The expected type
     * @param targetClass The target class
     * @return The deserialized object
     * @throws XMLSerializationException if deserialization fails
     */
    <T> T deserializeObject(Class<T> targetClass) throws XMLSerializationException;
    
    /**
     * Checks if the current element has any child elements.
     * 
     * @return true if has child elements, false otherwise
     */
    boolean hasChildElements();
    
    /**
     * Gets the number of child elements.
     * 
     * @return The number of child elements
     */
    int getChildElementCount();
}
