/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Concrete implementation of XMLDeserializerContext using W3C DOM.
 * This implementation parses XML documents using standard DOM APIs.
 */
public class DOMXMLDeserializerContext implements XMLDeserializerContext {
    
    private Document document;
    private Stack<Element> elementStack;
    
    /**
     * Creates a new deserializer context with no document.
     */
    public DOMXMLDeserializerContext() {
        this.elementStack = new Stack<>();
    }
    
    /**
     * Creates a new deserializer context and parses the specified file.
     * 
     * @param file The XML file to parse
     * @throws XMLSerializationException if parsing fails
     */
    public DOMXMLDeserializerContext(File file) throws XMLSerializationException {
        this();
        parseDocument(file);
    }
    
    /**
     * Creates a new deserializer context and parses the specified input stream.
     * 
     * @param inputStream The XML input stream to parse
     * @throws XMLSerializationException if parsing fails
     */
    public DOMXMLDeserializerContext(InputStream inputStream) throws XMLSerializationException {
        this();
        parseDocument(inputStream);
    }
    
    /**
     * Creates a new deserializer context with the specified document.
     * 
     * @param document The DOM document to deserialize
     */
    public DOMXMLDeserializerContext(Document document) {
        this();
        this.document = document;
        if (document != null) {
            elementStack.push(document.getDocumentElement());
        }
    }
    
    /**
     * Parses an XML file.
     * 
     * @param file The file to parse
     * @throws XMLSerializationException if parsing fails
     */
    private void parseDocument(File file) throws XMLSerializationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file);
            document.getDocumentElement().normalize();
            elementStack.push(document.getDocumentElement());
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to parse XML file: " + file, e);
        }
    }
    
    /**
     * Parses an XML input stream.
     * 
     * @param inputStream The input stream to parse
     * @throws XMLSerializationException if parsing fails
     */
    private void parseDocument(InputStream inputStream) throws XMLSerializationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            elementStack.push(document.getDocumentElement());
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to parse XML input stream", e);
        }
    }
    
    /**
     * Gets the DOM document being parsed.
     * 
     * @return The document
     */
    public Document getDocument() {
        return document;
    }
    
    @Override
    public Element getCurrentElement() {
        if (elementStack.isEmpty()) {
            return null;
        }
        return elementStack.peek();
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
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && 
                    name.equals(node.getNodeName())) {
                    return (Element) node;
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
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && 
                    name.equals(node.getNodeName())) {
                    result.add((Element) node);
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
                    elementStack.push((Element) node);
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean moveToNextSibling() {
        if (elementStack.isEmpty()) {
            return false;
        }
        
        Element current = elementStack.peek();
        Node next = current.getNextSibling();
        while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
            next = next.getNextSibling();
        }
        if (next != null) {
            elementStack.pop();
            elementStack.push((Element) next);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean moveToParent() {
        if (elementStack.size() > 1) {
            elementStack.pop();
            return true;
        }
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserializeObject(Class<T> targetClass) throws XMLSerializationException {
        try {
            // Create a new instance and deserialize it
            T object = targetClass.getDeclaredConstructor().newInstance();
            if (object instanceof XMLSerializable) {
                XMLSerializable serializable = (XMLSerializable) object;
                serializable.deserialize(this);
                return object;
            }
            throw new XMLSerializationException(
                "Target class does not implement XMLSerializable: " + targetClass.getName());
        } catch (Exception e) {
            throw new XMLSerializationException(
                "Failed to deserialize object of type " + targetClass.getName(), e);
        }
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
