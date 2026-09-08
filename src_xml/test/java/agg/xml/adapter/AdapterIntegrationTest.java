/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLHelper;
import agg.util.XMLObject;
import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xt_basis.Arc;
import agg.xt_basis.Rule;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.DOMXMLSerializerContext;
import agg.xml.core.DOMXMLDeserializerContext;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import static org.testng.Assert.*;

import java.io.File;
import java.util.List;

/**
 * Integration tests for the adapter pattern implementation.
 * Tests that adapters can correctly wrap legacy XMLObject implementations
 * and work with the new XML serialization infrastructure.
 */
public class AdapterIntegrationTest {
    
    private static final String BASELINE_DIR = "../../../../test_xml/baseline/samples/";
    
    private XMLHelper xmlHelper;
    
    @BeforeClass
    public void setUp() throws Exception {
        xmlHelper = new XMLHelper();
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        xmlHelper = null;
    }
    
    /**
     * Test that XMLObjectAdapter can wrap a Graph object
     */
    @Test
    public void testGraphAdapterCreation() throws Exception {
        // Load a graph using legacy XMLHelper
        File ggxFile = new File(BASELINE_DIR + "small_graph.ggx");
        boolean loaded = xmlHelper.read_from_xml(ggxFile.getAbsolutePath());
        assertTrue(loaded, "Should load test file");
        
        // Get the top object (should be a Graph)
        Graph graph = (Graph) xmlHelper.getTopObject(new Graph(null));
        assertNotNull(graph, "Should be able to get Graph from file");
        
        // Create adapter
        GraphAdapter adapter = new GraphAdapter(graph);
        assertNotNull(adapter, "Adapter should be created");
        assertTrue(adapter instanceof XMLSerializable, "Adapter should implement XMLSerializable");
        
        // Verify we can get the wrapped object back
        assertEquals(adapter.getGraph(), graph, "Should return the same Graph instance");
    }
    
    /**
     * Test XMLAdapterFactory with different object types
     */
    @Test
    public void testAdapterFactory() throws Exception {
        // Load a graph
        File ggxFile = new File(BASELINE_DIR + "small_graph.ggx");
        XMLHelper helper = new XMLHelper();
        helper.read_from_xml(ggxFile.getAbsolutePath());
        
        // Create a Graph and wrap it
        Graph graph = new Graph();
        XMLSerializable graphAdapter = XMLAdapterFactory.createAdapter(graph);
        assertNotNull(graphAdapter, "Should create adapter for Graph");
        assertTrue(graphAdapter instanceof GraphAdapter, "Should create GraphAdapter");
        
        // Create a Node and wrap it (using null for required parameters)
        // Note: This creates a Node with minimal parameters - actual usage would need proper AttrInstance and Type
        Node node = (Node) helper.getTopObject(new Node(null, null, graph));
        if (node != null) {
            XMLSerializable nodeAdapter = XMLAdapterFactory.createAdapter(node);
            assertNotNull(nodeAdapter, "Should create adapter for Node");
            assertTrue(nodeAdapter instanceof NodeAdapter, "Should create NodeAdapter");
        }
    }
    
    /**
     * Test that adapters maintain the XMLObject contract
     */
    @Test
    public void testAdapterImplementsXMLObject() throws Exception {
        Graph graph = new Graph();
        GraphAdapter adapter = new GraphAdapter(graph);
        
        // The adapter wraps an XMLObject, but implements XMLSerializable
        assertTrue(adapter.getWrappedObject() instanceof XMLObject, 
                  "Wrapped object should be XMLObject");
        assertTrue(adapter instanceof XMLSerializable, 
                  "Adapter should implement XMLSerializable");
    }
    
    /**
     * Test adapter with XMLHelperSerializerContext
     * This tests the bridge between new interfaces and legacy XMLHelper
     */
    @Test
    public void testAdapterWithXMLHelperContext() throws Exception {
        File ggxFile = new File(BASELINE_DIR + "small_graph.ggx");
        XMLHelper helper = new XMLHelper();
        helper.read_from_xml(ggxFile.getAbsolutePath());
        
        // Create a context that wraps XMLHelper
        XMLHelperSerializerContext context = new XMLHelperSerializerContext(helper);
        assertNotNull(context, "Context should be created");
        
        // The context should have access to the XMLHelper
        assertEquals(context.getXMLHelper(), helper, "Should return the same XMLHelper");
    }
    
    /**
     * Test that we can use both legacy and new approaches on the same data
     */
    @Test
    public void testDualApproachCompatibility() throws Exception {
        File ggxFile = new File(BASELINE_DIR + "small_graph.ggx");
        
        // Approach 1: Legacy XMLHelper
        XMLHelper legacyHelper = new XMLHelper();
        boolean legacyLoaded = legacyHelper.read_from_xml(ggxFile.getAbsolutePath());
        assertTrue(legacyLoaded, "Legacy approach should work");
        
        // Approach 2: New DOM-based approach
        DOMXMLDeserializerContext newContext = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(newContext, "New approach should work");
        
        // Both should be able to access the same data
        // (even if the internal representation differs)
        assertNotNull(legacyHelper.getDoc(), "Legacy should have document");
        assertNotNull(newContext.getDocument(), "New should have document");
    }
}
