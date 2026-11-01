/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import agg.xml.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Stack;

/**
 * Concrete implementation of XMLSerializerContext using W3C DOM.
 * This implementation creates and manages an XML document using standard DOM APIs.
 */
public class DOMXMLSerializerContext implements XMLSerializerContext {
    
    private final Document document;
    private final Stack<Element> elementStack;
    
    /**
     * Creates a new serializer context with a new document.
     */
    public DOMXMLSerializerContext() {
        this(createNewDocument());
    }
    
    /**
     * Creates a new serializer context with the specified document.
     * 
     * @param document The DOM document to use
     */
    public DOMXMLSerializerContext(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.document = document;
        this.elementStack = new Stack<>();

        Element root = document.getDocumentElement();
        if (root == null) {
            // Create root element for new documents
            root = document.createElement("Document");
            root.setAttribute("version", "1.0");
            document.appendChild(root);
        }
        elementStack.push(root);
    }
    
    /**
     * Creates a new DOM document.
     * 
     * @return A new Document instance
     */
    private static Document createNewDocument() {
        try {
            DocumentBuilderFactory factory = XMLUtils.createSecureDocumentBuilderFactory(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create DOM document", e);
        }
    }
    
    /**
     * Gets the DOM document being built.
     * 
     * @return The document
     */
    public Document getDocument() {
        return document;
    }
    
    @Override
    public Element getCurrentElement() {
        if (elementStack.isEmpty()) {
            return document.getDocumentElement();
        }
        return elementStack.peek();
    }
    
    @Override
    public Element createElement(String name) {
        return document.createElement(name);
    }
    
    @Override
    public Element createAndAppendElement(String name) {
        Element parent = getCurrentElement();
        if (parent == null) {
            throw new IllegalStateException("No current element to append to");
        }
        Element element = document.createElement(name);
        parent.appendChild(element);
        return element;
    }
    
    @Override
    public void setAttribute(String name, String value) {
        Element current = getCurrentElement();
        if (current != null && value != null) {
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
        if (current != null && text != null) {
            current.setTextContent(text);
        }
    }
    
    @Override
    public void serializeObject(Object object) throws XMLSerializationException {
        if (object == null) {
            return;
        }
        
        if (object instanceof XMLSerializable) {
            XMLSerializable serializable = (XMLSerializable) object;
            serializable.serialize(this);
        } else {
            // For non-XMLSerializable objects, just convert to string
            setTextContent(object.toString());
        }
    }
    
    @Override
    public void pushElement(Element element) {
        if (element != null) {
            elementStack.push(element);
        }
    }
    
    @Override
    public Element popElement() {
        if (elementStack.isEmpty()) {
            return null;
        }
        return elementStack.pop();
    }
    
    @Override
    public Element getRootElement() {
        return document.getDocumentElement();
    }
    
    @Override
    public String toXMLString() throws XMLSerializationException {
        try {
            // Use a transformer to convert document to string
            javax.xml.transform.TransformerFactory tf = 
                javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            
            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(
                new javax.xml.transform.dom.DOMSource(document),
                new javax.xml.transform.stream.StreamResult(writer)
            );
            return writer.toString();
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to convert document to XML string", e);
        }
    }
}
