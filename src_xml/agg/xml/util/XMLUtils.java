/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing static helper methods for DOM-based XML operations.
 *
 * <p>This class centralizes common XML operations such as document creation,
 * element creation, attribute reading, and child element access, reducing
 * boilerplate code in adapter and context implementations.</p>
 */
public final class XMLUtils {

    private XMLUtils() {
    }

    /**
     * Creates a DocumentBuilderFactory with XXE protection enabled.
     *
     * <p>Disables external entities, external DTDs, and enables secure processing
     * to prevent XXE injection attacks when parsing untrusted XML files.</p>
     *
     * @param namespaceAware whether the factory should be namespace-aware
     * @return a hardened DocumentBuilderFactory
     * @throws ParserConfigurationException if a feature cannot be set
     */
    public static DocumentBuilderFactory createSecureDocumentBuilderFactory(boolean namespaceAware)
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(namespaceAware);
        // Best-effort XXE hardening: set features that may not be supported by all parsers
        setFeatureIfSupported(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeatureIfSupported(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }

    private static void setFeatureIfSupported(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (ParserConfigurationException ignored) {
            // Feature not supported by this parser -- safe to skip
        }
    }

    /**
     * Creates a DocumentBuilderFactory with XXE protection enabled (namespace-aware).
     *
     * @return a hardened DocumentBuilderFactory
     * @throws ParserConfigurationException if a feature cannot be set
     */
    public static DocumentBuilderFactory createSecureDocumentBuilderFactory()
            throws ParserConfigurationException {
        return createSecureDocumentBuilderFactory(true);
    }

    /**
     * Creates a new empty DOM document.
     *
     * @return A new Document instance
     * @throws RuntimeException if document creation fails
     */
    public static Document createDocument() {
        try {
            DocumentBuilderFactory factory = createSecureDocumentBuilderFactory(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to create DOM document", e);
        }
    }

    /**
     * Creates a new element in the specified document.
     *
     * @param doc  The owner document
     * @param name The element name
     * @return The newly created element
     */
    public static Element createElement(Document doc, String name) {
        if (doc == null || name == null) {
            throw new IllegalArgumentException("Document and name must not be null");
        }
        return doc.createElement(name);
    }

    /**
     * Creates a new element with text content in the specified document.
     *
     * @param doc  The owner document
     * @param name The element name
     * @param text The text content
     * @return The newly created element with text content
     */
    public static Element createElementWithText(Document doc, String name, String text) {
        Element element = createElement(doc, name);
        if (text != null) {
            element.setTextContent(text);
        }
        return element;
    }

    /**
     * Creates a new element, appends it to a parent, and returns it.
     *
     * @param parent The parent element
     * @param name   The element name
     * @return The newly created and appended element
     */
    public static Element createAndAppendElement(Element parent, String name) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent element must not be null");
        }
        Document doc = parent.getOwnerDocument();
        Element element = doc.createElement(name);
        parent.appendChild(element);
        return element;
    }

    /**
     * Sets an attribute on an element, skipping null values.
     *
     * @param element The element
     * @param name    The attribute name
     * @param value   The attribute value (skipped if null)
     */
    public static void setAttribute(Element element, String name, String value) {
        if (element != null && name != null && value != null) {
            element.setAttribute(name, value);
        }
    }

    /**
     * Sets a boolean attribute on an element.
     *
     * @param element The element
     * @param name    The attribute name
     * @param value   The boolean value
     */
    public static void setAttribute(Element element, String name, boolean value) {
        setAttribute(element, name, String.valueOf(value));
    }

    /**
     * Sets an integer attribute on an element.
     *
     * @param element The element
     * @param name    The attribute name
     * @param value   The integer value
     */
    public static void setAttribute(Element element, String name, int value) {
        setAttribute(element, name, String.valueOf(value));
    }

    /**
     * Sets a long attribute on an element.
     *
     * @param element The element
     * @param name    The attribute name
     * @param value   The long value
     */
    public static void setAttribute(Element element, String name, long value) {
        setAttribute(element, name, String.valueOf(value));
    }

    /**
     * Sets a double attribute on an element.
     *
     * @param element The element
     * @param name    The attribute name
     * @param value   The double value
     */
    public static void setAttribute(Element element, String name, double value) {
        setAttribute(element, name, String.valueOf(value));
    }

    /**
     * Gets the text content of an element, trimmed.
     *
     * @param element The element
     * @return The trimmed text content, or null if the element is null or empty
     */
    public static String getTextContent(Element element) {
        if (element == null) {
            return null;
        }
        String text = element.getTextContent();
        if (text != null) {
            text = text.trim();
            if (text.isEmpty()) {
                return null;
            }
        }
        return text;
    }

    /**
     * Gets an attribute value from an element.
     *
     * @param element The element
     * @param name    The attribute name
     * @return The attribute value, or null if not present
     */
    public static String getAttribute(Element element, String name) {
        if (element == null || name == null) {
            return null;
        }
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return null;
    }

    /**
     * Gets an integer attribute from an element.
     *
     * @param element      The element
     * @param name         The attribute name
     * @param defaultValue The default value if attribute is not present or invalid
     * @return The integer value
     */
    public static int getIntAttribute(Element element, String name, int defaultValue) {
        String value = getAttribute(element, name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets a long attribute from an element.
     *
     * @param element      The element
     * @param name         The attribute name
     * @param defaultValue The default value if attribute is not present or invalid
     * @return The long value
     */
    public static long getLongAttribute(Element element, String name, long defaultValue) {
        String value = getAttribute(element, name);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets a double attribute from an element.
     *
     * @param element      The element
     * @param name         The attribute name
     * @param defaultValue The default value if attribute is not present or invalid
     * @return The double value
     */
    public static double getDoubleAttribute(Element element, String name, double defaultValue) {
        String value = getAttribute(element, name);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets a boolean attribute from an element.
     *
     * @param element      The element
     * @param name         The attribute name
     * @param defaultValue The default value if attribute is not present
     * @return The boolean value
     */
    public static boolean getBooleanAttribute(Element element, String name, boolean defaultValue) {
        String value = getAttribute(element, name);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Gets all direct child elements with the specified name.
     *
     * @param element   The parent element
     * @param childName The child element name
     * @return A list of matching child elements (empty if none found)
     */
    public static List<Element> getChildElements(Element element, String childName) {
        List<Element> result = new ArrayList<>();
        if (element == null) {
            return result;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && (childName == null || childName.equals(node.getNodeName()))) {
                result.add((Element) node);
            }
        }
        return result;
    }

    /**
     * Gets the first direct child element with the specified name.
     *
     * @param element   The parent element
     * @param childName The child element name
     * @return The first matching child element, or null if not found
     */
    public static Element getFirstChildElement(Element element, String childName) {
        if (element == null) {
            return null;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && childName.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    /**
     * Gets the first direct child element regardless of name.
     *
     * @param element The parent element
     * @return The first child element, or null if none
     */
    public static Element getFirstChildElement(Element element) {
        if (element == null) {
            return null;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }

    /**
     * Counts the number of direct child elements.
     *
     * @param element The parent element
     * @return The number of child elements
     */
    public static int getChildElementCount(Element element) {
        if (element == null) {
            return 0;
        }
        int count = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }
}
