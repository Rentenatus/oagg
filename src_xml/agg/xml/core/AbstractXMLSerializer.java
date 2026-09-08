/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract base class for XML serializers that provides common functionality.
 * This class implements the XMLSerializer interface and provides basic
 * serialization infrastructure.
 */
public abstract class AbstractXMLSerializer implements XMLSerializer {
    
    /**
     * Creates a new element with the specified name and adds it to the parent.
     * 
     * @param parent The parent element
     * @param name The element name
     * @return The newly created element
     */
    protected Element createElement(Element parent, String name) {
        Document doc = parent.getOwnerDocument();
        Element element = doc.createElement(name);
        parent.appendChild(element);
        return element;
    }
    
    /**
     * Sets an attribute on the specified element.
     * 
     * @param element The element
     * @param name The attribute name
     * @param value The attribute value
     */
    protected void setAttribute(Element element, String name, String value) {
        if (value != null) {
            element.setAttribute(name, value);
        }
    }
    
    /**
     * Sets a boolean attribute on the specified element.
     * 
     * @param element The element
     * @param name The attribute name
     * @param value The boolean value
     */
    protected void setAttribute(Element element, String name, boolean value) {
        setAttribute(element, name, String.valueOf(value));
    }
    
    /**
     * Sets an integer attribute on the specified element.
     * 
     * @param element The element
     * @param name The attribute name
     * @param value The integer value
     */
    protected void setAttribute(Element element, String name, int value) {
        setAttribute(element, name, String.valueOf(value));
    }
    
    /**
     * Sets a double attribute on the specified element.
     * 
     * @param element The element
     * @param name The attribute name
     * @param value The double value
     */
    protected void setAttribute(Element element, String name, double value) {
        setAttribute(element, name, String.valueOf(value));
    }
    
    /**
     * Sets text content for the specified element.
     * 
     * @param element The element
     * @param text The text content
     */
    protected void setTextContent(Element element, String text) {
        if (text != null && !text.isEmpty()) {
            element.setTextContent(text);
        }
    }
}
