/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

import org.w3c.dom.Element;

/**
 * Unit tests for DOM-based XML serialization contexts.
 */
public class DOMXMLContextTest {
    
    private DOMXMLSerializerContext serializerContext;
    
    @BeforeMethod
    public void setUp() {
        serializerContext = new DOMXMLSerializerContext();
    }
    
    @Test
    public void testSerializerContextCreation() {
        assertNotNull(serializerContext, "Serializer context should not be null");
    }
    
    @Test
    public void testGetCurrentElement() {
        Element current = serializerContext.getCurrentElement();
        assertNotNull(current, "Current element should not be null");
        assertEquals(current.getNodeName(), "Document", "Current element should be Document");
    }
    
    @Test
    public void testCreateAndAppendElement() {
        Element child = serializerContext.createAndAppendElement("TestElement");
        assertNotNull(child, "Child element should not be null");
        assertEquals(child.getNodeName(), "TestElement", "Child element name should be TestElement");
        
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getNodeName(), "Document", "Current element should be Document");
    }
    
    @Test
    public void testSetAttribute() {
        serializerContext.setAttribute("testAttr", "testValue");
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getAttribute("testAttr"), "testValue", "Attribute should be set");
    }
    
    @Test
    public void testSetBooleanAttribute() {
        serializerContext.setAttribute("boolAttr", true);
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getAttribute("boolAttr"), "true", "Boolean attribute should be set");
    }
    
    @Test
    public void testSetIntAttribute() {
        serializerContext.setAttribute("intAttr", 42);
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getAttribute("intAttr"), "42", "Int attribute should be set");
    }
    
    @Test
    public void testSetDoubleAttribute() {
        serializerContext.setAttribute("doubleAttr", 3.14);
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getAttribute("doubleAttr"), "3.14", "Double attribute should be set");
    }
    
    @Test
    public void testSetTextContent() {
        serializerContext.setTextContent("Hello World");
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getTextContent(), "Hello World", "Text content should be set");
    }
    
    @Test
    public void testPushAndPopElement() {
        Element child = serializerContext.createAndAppendElement("Child");
        serializerContext.pushElement(child);
        
        Element current = serializerContext.getCurrentElement();
        assertEquals(current.getNodeName(), "Child", "Current element should be Child");
        
        Element popped = serializerContext.popElement();
        assertEquals(popped.getNodeName(), "Child", "Popped element should be Child");
        
        current = serializerContext.getCurrentElement();
        assertEquals(current.getNodeName(), "Document", "Current element should be Document again");
    }
    
    @Test
    public void testGetRootElement() {
        Element root = serializerContext.getRootElement();
        assertNotNull(root, "Root element should not be null");
        assertEquals(root.getNodeName(), "Document", "Root element should be Document");
    }
    
    @Test
    public void testToXMLString() throws XMLSerializationException {
        serializerContext.setAttribute("version", "2.0");
        serializerContext.createAndAppendElement("Graph");
        
        String xml = serializerContext.toXMLString();
        assertNotNull(xml, "XML string should not be null");
        assertTrue(xml.contains("Document"), "XML should contain Document");
        assertTrue(xml.contains("version"), "XML should contain version");
        assertTrue(xml.contains("Graph"), "XML should contain Graph");
    }
    
    @Test
    public void testDeserializerContext() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document version=\"1.0\">\n" +
                     "  <Graph name=\"test\" nodes=\"5\"/>\n" +
                     "</Document>";
        
        java.io.ByteArrayInputStream inputStream = 
            new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        DOMXMLDeserializerContext deserializerContext = 
            new DOMXMLDeserializerContext(inputStream);
        
        assertNotNull(deserializerContext, "Deserializer context should not be null");
        
        Element current = deserializerContext.getCurrentElement();
        assertNotNull(current, "Current element should not be null");
        assertEquals(current.getNodeName(), "Document", "Current element should be Document");
    }
    
    @Test
    public void testDeserializerGetAttribute() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<Document version=\"1.0\">\n" +
                     "  <Graph name=\"test\" nodes=\"5\"/>\n" +
                     "</Document>";
        
        java.io.ByteArrayInputStream inputStream = 
            new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        DOMXMLDeserializerContext deserializerContext = 
            new DOMXMLDeserializerContext(inputStream);
        
        deserializerContext.moveToFirstChild();
        
        String name = deserializerContext.getAttribute("name");
        assertEquals(name, "test", "Name attribute should be test");
        
        int nodes = deserializerContext.getAttribute("nodes", 0);
        assertEquals(nodes, 5, "Nodes attribute should be 5");
    }
}
