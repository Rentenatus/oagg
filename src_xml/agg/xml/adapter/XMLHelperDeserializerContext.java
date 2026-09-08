/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLHelper;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of XMLDeserializerContext that wraps an XMLHelper instance.
 * This allows the new deserialization infrastructure to work with the existing
 * XMLHelper-based code.
 */
public class XMLHelperDeserializerContext implements XMLDeserializerContext {
    
    private final XMLHelper xmlHelper;
    
    /**
     * Creates a new context wrapping the specified XMLHelper.
     * 
     * @param xmlHelper The XMLHelper instance to wrap
     */
    public XMLHelperDeserializerContext(XMLHelper xmlHelper) {
        if (xmlHelper == null) {
            throw new IllegalArgumentException("XMLHelper cannot be null");
        }
        this.xmlHelper = xmlHelper;
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
        return xmlHelper.top();
    }
    
    @Override
    public String getCurrentElementName() {
        Element current = getCurrentElement();
        if (current != null) {
            return current.getNodeName();
        }
        return null;
    }
    
    @Override
    public String getAttribute(String name) {
        Element current = getCurrentElement();
        if (current != null && current.hasAttribute(name)) {
            return current.getAttribute(name);
        }
        return null;
    }
    
    @Override
    public boolean getAttribute(String name, boolean defaultValue) {
        String value = getAttribute(name);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    @Override
    public int getAttribute(String name, int defaultValue) {
        String value = getAttribute(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    @Override
    public double getAttribute(String name, double defaultValue) {
        String value = getAttribute(name);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    @Override
    public String getTextContent() {
        Element current = getCurrentElement();
        if (current != null) {
            StringBuilder text = new StringBuilder();
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    text.append(node.getNodeValue());
                }
            }
            return text.toString().trim();
        }
        return null;
    }
    
    @Override
    public Element getFirstChildElement(String name) {
        Element current = getCurrentElement();
        if (current != null) {
            NodeList children = current.getElementsByTagName(name);
            if (children.getLength() > 0) {
                Node node = children.item(0);
                // Check if this node is a direct child
                NodeList allChildren = current.getChildNodes();
                for (int i = 0; i < allChildren.getLength(); i++) {
                    Node child = allChildren.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && 
                        name.equals(child.getNodeName())) {
                        return (Element) child;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public Element[] getChildElements(String name) {
        Element current = getCurrentElement();
        if (current != null) {
            List<Element> result = new ArrayList<>();
            NodeList allChildren = current.getChildNodes();
            for (int i = 0; i < allChildren.getLength(); i++) {
                Node child = allChildren.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && 
                    name.equals(child.getNodeName())) {
                    result.add((Element) child);
                }
            }
            return result.toArray(new Element[0]);
        }
        return new Element[0];
    }
    
    @Override
    public boolean moveToFirstChild() {
        Element current = getCurrentElement();
        if (current != null) {
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    xmlHelper.push(node);
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean moveToNextSibling() {
        Element current = getCurrentElement();
        if (current != null) {
            Node next = current.getNextSibling();
            while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
                next = next.getNextSibling();
            }
            if (next != null) {
                xmlHelper.push(next);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean moveToParent() {
        // Note: xmlHelper.pop() is private, so we cannot call it directly
        // For now, we just return false to indicate we cannot move to parent
        // In a full implementation, we would need a different approach
        return false;
    }
    
    @Override
    public <T> T deserializeObject(Class<T> targetClass) throws XMLSerializationException {
        // For now, this is a placeholder
        // In a full implementation, we would use reflection or a registry
        // to create and deserialize the appropriate object
        throw new XMLSerializationException("deserializeObject not yet implemented");
    }
    
    @Override
    public boolean hasChildElements() {
        Element current = getCurrentElement();
        if (current != null) {
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int getChildElementCount() {
        Element current = getCurrentElement();
        if (current != null) {
            int count = 0;
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }
}
