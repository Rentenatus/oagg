/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.util;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.testng.Assert.*;

import java.util.List;

/**
 * Unit tests for {@link XMLUtils}.
 */
public class XMLUtilsTest {

    private Document doc;

    @BeforeMethod
    public void setUp() {
        doc = XMLUtils.createDocument();
    }

    @Test
    public void testCreateDocument() {
        assertNotNull(doc, "Document should not be null");
    }

    @Test
    public void testCreateElement() {
        Element element = XMLUtils.createElement(doc, "Test");
        assertNotNull(element, "Element should not be null");
        assertEquals(element.getNodeName(), "Test", "Element name should be Test");
    }

    @Test
    public void testCreateElementWithText() {
        Element element = XMLUtils.createElementWithText(doc, "Test", "hello");
        assertEquals(element.getTextContent(), "hello", "Text content should be hello");
    }

    @Test
    public void testCreateAndAppendElement() {
        Element parent = doc.createElement("Parent");
        doc.appendChild(parent);
        Element child = XMLUtils.createAndAppendElement(parent, "Child");
        assertNotNull(child, "Child should not be null");
        assertEquals(child.getParentNode(), parent, "Child parent should be Parent");
    }

    @Test
    public void testSetAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        XMLUtils.setAttribute(element, "attr", "value");
        assertEquals(element.getAttribute("attr"), "value", "Attribute should be set");
    }

    @Test
    public void testSetAttributeNullSkipped() {
        Element element = XMLUtils.createElement(doc, "Test");
        XMLUtils.setAttribute(element, "attr", null);
        assertFalse(element.hasAttribute("attr"), "Null attribute should be skipped");
    }

    @Test
    public void testSetIntAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        XMLUtils.setAttribute(element, "count", 42);
        assertEquals(element.getAttribute("count"), "42", "Int attribute should be 42");
    }

    @Test
    public void testSetBooleanAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        XMLUtils.setAttribute(element, "flag", true);
        assertEquals(element.getAttribute("flag"), "true", "Boolean attribute should be true");
    }

    @Test
    public void testSetDoubleAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        XMLUtils.setAttribute(element, "val", 3.14);
        assertEquals(element.getAttribute("val"), "3.14", "Double attribute should be 3.14");
    }

    @Test
    public void testGetAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        element.setAttribute("attr", "value");
        assertEquals(XMLUtils.getAttribute(element, "attr"), "value", "Should get attribute");
    }

    @Test
    public void testGetAttributeNotPresent() {
        Element element = XMLUtils.createElement(doc, "Test");
        assertNull(XMLUtils.getAttribute(element, "nonexistent"), "Should return null");
    }

    @Test
    public void testGetIntAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        element.setAttribute("num", "123");
        assertEquals(XMLUtils.getIntAttribute(element, "num", 0), 123, "Should get 123");
    }

    @Test
    public void testGetIntAttributeWithDefault() {
        Element element = XMLUtils.createElement(doc, "Test");
        assertEquals(XMLUtils.getIntAttribute(element, "num", 99), 99, "Should get default 99");
    }

    @Test
    public void testGetIntAttributeInvalid() {
        Element element = XMLUtils.createElement(doc, "Test");
        element.setAttribute("num", "abc");
        assertEquals(XMLUtils.getIntAttribute(element, "num", 0), 0, "Should get default for invalid");
    }

    @Test
    public void testGetBooleanAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        element.setAttribute("flag", "true");
        assertTrue(XMLUtils.getBooleanAttribute(element, "flag", false), "Should be true");
    }

    @Test
    public void testGetBooleanAttributeDefault() {
        Element element = XMLUtils.createElement(doc, "Test");
        assertFalse(XMLUtils.getBooleanAttribute(element, "flag", false), "Should get default false");
    }

    @Test
    public void testGetDoubleAttribute() {
        Element element = XMLUtils.createElement(doc, "Test");
        element.setAttribute("val", "3.14");
        assertEquals(XMLUtils.getDoubleAttribute(element, "val", 0.0), 3.14, 0.001, "Should get 3.14");
    }

    @Test
    public void testGetTextContent() {
        Element element = XMLUtils.createElementWithText(doc, "Test", "  hello  ");
        assertEquals(XMLUtils.getTextContent(element), "hello", "Should be trimmed");
    }

    @Test
    public void testGetTextContentEmpty() {
        Element element = XMLUtils.createElement(doc, "Test");
        assertNull(XMLUtils.getTextContent(element), "Empty text should return null");
    }

    @Test
    public void testGetChildElements() {
        Element parent = doc.createElement("Parent");
        parent.appendChild(doc.createElement("Child"));
        parent.appendChild(doc.createElement("Child"));
        parent.appendChild(doc.createElement("Other"));
        List<Element> children = XMLUtils.getChildElements(parent, "Child");
        assertEquals(children.size(), 2, "Should find 2 Child elements");
    }

    @Test
    public void testGetFirstChildElementByName() {
        Element parent = doc.createElement("Parent");
        Element child = doc.createElement("Child");
        parent.appendChild(child);
        Element found = XMLUtils.getFirstChildElement(parent, "Child");
        assertNotNull(found, "Should find child");
        assertEquals(found.getNodeName(), "Child", "Should be Child");
    }

    @Test
    public void testGetFirstChildElementNotFound() {
        Element parent = doc.createElement("Parent");
        assertNull(XMLUtils.getFirstChildElement(parent, "NonExistent"), "Should return null");
    }

    @Test
    public void testGetFirstChildElementAny() {
        Element parent = doc.createElement("Parent");
        parent.appendChild(doc.createElement("Child1"));
        parent.appendChild(doc.createElement("Child2"));
        Element first = XMLUtils.getFirstChildElement(parent);
        assertNotNull(first, "Should find first child");
        assertEquals(first.getNodeName(), "Child1", "Should be Child1");
    }

    @Test
    public void testGetChildElementCount() {
        Element parent = doc.createElement("Parent");
        parent.appendChild(doc.createElement("Child1"));
        parent.appendChild(doc.createElement("Child2"));
        parent.appendChild(doc.createTextNode("text"));
        assertEquals(XMLUtils.getChildElementCount(parent), 2, "Should count 2 elements");
    }
}
