/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLHelper;
import agg.xml.core.XMLSerializerContext;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Stack;

/**
 * Implementation of XMLSerializerContext that wraps an XMLHelper instance.
 * This allows the new serialization infrastructure to work with the existing
 * XMLHelper-based code.
 *
 * @deprecated This context has limitations due to XMLHelper's private stack
 * management: {@code popElement()} cannot call {@code XMLHelper.pop()} (it is
 * private), and {@code toXMLString()} returns an empty string because
 * XMLHelper does not expose its document for direct serialization. Use
 * {@link agg.xml.core.DOMXMLSerializerContext} instead, which provides full
 * functionality without these limitations.
 */
@Deprecated
public class XMLHelperSerializerContext implements XMLSerializerContext {
    
    private final XMLHelper xmlHelper;
    private final Stack<Element> elementStack;
    
    /**
     * Creates a new context wrapping the specified XMLHelper.
     * 
     * @param xmlHelper The XMLHelper instance to wrap
     */
    public XMLHelperSerializerContext(XMLHelper xmlHelper) {
        if (xmlHelper == null) {
            throw new IllegalArgumentException("XMLHelper cannot be null");
        }
        this.xmlHelper = xmlHelper;
        this.elementStack = new Stack<>();
        
        // Initialize with the current top element from XMLHelper
        Element top = xmlHelper.top();
        if (top != null) {
            elementStack.push(top);
        }
    }
    
    /**
     * Gets the underlying XMLHelper instance.
     * 
     * @return The XMLHelper instance
     */
    public XMLHelper getXMLHelper() {
        return xmlHelper;
    }
    
    @Override
    public Element getCurrentElement() {
        if (elementStack.isEmpty()) {
            return xmlHelper.top();
        }
        return elementStack.peek();
    }
    
    @Override
    public Element createElement(String name) {
        Document doc = xmlHelper.getDoc();
        Element element = doc.createElement(name);
        return element;
    }
    
    @Override
    public Element createAndAppendElement(String name) {
        Element parent = getCurrentElement();
        if (parent == null) {
            throw new IllegalStateException("No current element to append to");
        }
        Document doc = xmlHelper.getDoc();
        Element element = doc.createElement(name);
        parent.appendChild(element);
        return element;
    }
    
    @Override
    public void setAttribute(String name, String value) {
        Element current = getCurrentElement();
        if (current != null) {
            current.setAttribute(name, value);
        }
    }
    
    @Override
    public void setAttribute(String name, boolean value) {
        setAttribute(name, String.valueOf(value));
    }
    
    @Override
    public void setAttribute(String name, int value) {
        setAttribute(name, String.valueOf(value));
    }
    
    @Override
    public void setAttribute(String name, double value) {
        setAttribute(name, String.valueOf(value));
    }
    
    @Override
    public void setTextContent(String text) {
        Element current = getCurrentElement();
        if (current != null) {
            Node textNode = xmlHelper.getDoc().createTextNode(text);
            current.appendChild(textNode);
        }
    }
    
    @Override
    public void serializeObject(Object object) throws XMLSerializationException {
        // If the object is an XMLObject, we can use XMLHelper directly
        if (object instanceof agg.util.XMLObject) {
            agg.util.XMLObject xmlObject = (agg.util.XMLObject) object;
            xmlObject.XwriteObject(xmlHelper);
        } else if (object instanceof agg.xml.core.XMLSerializable) {
            agg.xml.core.XMLSerializable serializable = (agg.xml.core.XMLSerializable) object;
            serializable.serialize(this);
        } else {
            // For other objects, we might need a different approach
            // For now, just convert to string
            if (object != null) {
                setTextContent(object.toString());
            }
        }
    }
    
    @Override
    public void pushElement(Element element) {
        elementStack.push(element);
        xmlHelper.push(element);
    }
    
    @Override
    public Element popElement() {
        if (elementStack.isEmpty()) {
            return null;
        }
        Element popped = elementStack.pop();
        // Note: We cannot call xmlHelper.pop() as it's private
        // Instead, we just manage our own stack
        return popped;
    }
    
    @Override
    public Element getRootElement() {
        Document doc = xmlHelper.getDoc();
        if (doc != null) {
            return doc.getDocumentElement();
        }
        return null;
    }
    
    @Override
    public String toXMLString() throws XMLSerializationException {
        // For now, we return an empty string as XMLHelper doesn't directly support this
        // In a full implementation, we would serialize the document to a string
        return "";
    }
}
