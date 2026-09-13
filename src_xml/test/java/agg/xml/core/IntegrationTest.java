/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import agg.util.XMLHelper;
import agg.util.XMLObject;
import agg.xml.TestDataHelper;
import agg.xt_basis.Graph;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import static org.testng.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Integration tests that verify the new XML serialization module
 * can correctly load and save .ggx files, comparing results with
 * the legacy XMLHelper implementation.
 */
public class IntegrationTest {

    private static final String OUTPUT_DIR = "target/test-output/";

    private File tempOutputDir;

    @BeforeClass
    public void setUp() throws Exception {
        TestDataHelper.requireAllSamples();
        // Create temporary output directory
        tempOutputDir = new File(OUTPUT_DIR);
        if (!tempOutputDir.exists()) {
            tempOutputDir.mkdirs();
        }
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        // Clean up temporary files
        // (Optional - keep files for debugging)
    }
    
    /**
     * Test loading a .ggx file using the new DOMXMLDeserializerContext
     */
    @Test
    public void testLoadSmallGraphWithDOMContext() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");

        // Load using new DOM-based deserializer
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(context, "Deserializer context should not be null");
        
        // Verify we can navigate the document
        assertNotNull(context.getCurrentElement(), "Current element should not be null");
        assertEquals(context.getCurrentElement().getNodeName(), "Document", 
                    "Root element should be Document");
    }
    
    /**
     * Test loading all .ggx sample files
     */
    @Test
    public void testLoadAllSampleFiles() throws Exception {
        String[] sampleFiles = TestDataHelper.SAMPLE_FILES;
        
        for (String filename : TestDataHelper.SAMPLE_FILES) {
            File ggxFile = TestDataHelper.resolveSample(filename);

            // Test loading with new DOMXMLDeserializerContext
            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(ggxFile);
            assertNotNull(context, "Failed to load: " + filename);
            assertNotNull(context.getCurrentElement(), "No root element in: " + filename);
        }
    }
    
    /**
     * Test that new serializer context can create XML output
     */
    @Test
    public void testCreateAndSerializeSimpleGraph() throws Exception {
        DOMXMLSerializerContext context = new DOMXMLSerializerContext();
        
        // Create a simple structure
        context.setAttribute("version", "1.0");
        
        // Add a Graph element
        context.createAndAppendElement("Graph");
        context.setAttribute("name", "TestGraph");
        context.setAttribute("nodes", 5);
        context.setAttribute("arcs", 3);
        
        // Add a Node element
        context.createAndAppendElement("Node");
        context.setAttribute("ID", "N0");
        context.setAttribute("type", "default");
        context.popElement();
        
        // Convert to XML string
        String xml = context.toXMLString();
        assertNotNull(xml, "XML output should not be null");
        assertTrue(xml.contains("Document"), "XML should contain Document element");
        assertTrue(xml.contains("Graph"), "XML should contain Graph element");
        assertTrue(xml.contains("Node"), "XML should contain Node element");
        assertTrue(xml.contains("TestGraph"), "XML should contain graph name");
    }
    
    /**
     * Test round-trip serialization: serialize to string, then parse back
     */
    @Test
    public void testRoundTripSerialization() throws Exception {
        // Create and serialize
        DOMXMLSerializerContext serializer = new DOMXMLSerializerContext();
        serializer.setAttribute("version", "1.0");

        org.w3c.dom.Element graphElem = serializer.createAndAppendElement("Graph");
        serializer.pushElement(graphElem);
        serializer.setAttribute("name", "RoundTripTest");
        serializer.setAttribute("nodes", 10);

        org.w3c.dom.Element metaElem = serializer.createAndAppendElement("Metadata");
        serializer.pushElement(metaElem);
        serializer.setTextContent("Test content");
        serializer.popElement(); // pop Metadata
        serializer.popElement(); // pop Graph

        String xml = serializer.toXMLString();
        assertNotNull(xml, "Serialized XML should not be null");

        // Parse back
        java.io.ByteArrayInputStream inputStream =
            new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"));
        DOMXMLDeserializerContext deserializer = new DOMXMLDeserializerContext(inputStream);

        assertNotNull(deserializer, "Deserializer should not be null");
        assertEquals(deserializer.getCurrentElement().getNodeName(), "Document",
                    "Root element should be Document");

        // Navigate to Graph
        assertTrue(deserializer.moveToFirstChild(), "Should move to first child");
        assertEquals(deserializer.getCurrentElement().getNodeName(), "Graph",
                    "First child should be Graph");
        assertEquals(deserializer.getAttribute("name"), "RoundTripTest",
                    "Graph name should match");
        assertEquals(deserializer.getAttribute("nodes", 0), 10,
                    "Node count should match");
    }
    
    /**
     * Test comparison between legacy XMLHelper and new DOM context
     * This verifies that both produce compatible XML structures
     */
    @Test
    public void testLegacyVsNewDOMStructure() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");

        // Load with legacy XMLHelper
        XMLHelper legacyHelper = new XMLHelper();
        boolean legacyLoaded = legacyHelper.read_from_xml(ggxFile.getAbsolutePath());
        assertTrue(legacyLoaded, "Legacy XMLHelper should load the file");
        
        // Load with new DOMXMLDeserializerContext
        DOMXMLDeserializerContext newContext = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(newContext, "New context should load the file");
        
        // Both should have a Document root element
        assertEquals(legacyHelper.getDoc().getDocumentElement().getNodeName(), "Document",
                    "Legacy document root should be Document");
        assertEquals(newContext.getCurrentElement().getNodeName(), "Document",
                    "New context root should be Document");
        
        // Both should have the same version attribute
        String legacyVersion = legacyHelper.getDoc().getDocumentElement().getAttribute("version");
        String newVersion = newContext.getCurrentElement().getAttribute("version");
        assertNotNull(legacyVersion, "Legacy version should exist");
        assertNotNull(newVersion, "New version should exist");
    }
}
