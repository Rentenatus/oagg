/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;
import agg.xt_basis.Graph;
import agg.xt_basis.GraGra;
import agg.xml.core.XMLSerializable;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests for the extended XMLAdapterFactory.
 * Verifies that the factory creates the correct adapter type for each domain object.
 */
public class AdapterFactoryTest {

    @Test
    public void testCreateGraphAdapter() {
        Graph graph = new Graph();
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(graph);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof GraphAdapter, "Should create GraphAdapter");
    }

    @Test
    public void testCreateGraGraAdapter() {
        GraGra graGra = new GraGra();
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(graGra);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof GraGraAdapter, "Should create GraGraAdapter");
    }

    @Test
    public void testCreateNullAdapter() {
        XMLSerializable adapter = XMLAdapterFactory.createAdapter((XMLObject) null);
        assertNull(adapter, "Should return null for null input");
    }

    @Test
    public void testCreateGenericAdapter() {
        XMLObject anonymousXmlObject = new XMLObject() {
            @Override
            public void XwriteObject(agg.util.XMLHelper h) {}
            @Override
            public void XreadObject(agg.util.XMLHelper h) {}
        };
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(anonymousXmlObject);
        assertNotNull(adapter, "Should create generic adapter");
        assertTrue(adapter instanceof XMLObjectAdapter, "Should create generic XMLObjectAdapter");
    }

    @Test
    public void testAllAdaptersImplementXMLSerializable() {
        Graph graph = new Graph();
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(graph);
        assertTrue(adapter instanceof XMLSerializable, "Adapter should implement XMLSerializable");
    }

    @Test
    public void testSpecificFactoryMethods() {
        Graph graph = new Graph();
        assertNotNull(XMLAdapterFactory.createGraphAdapter(graph), "createGraphAdapter should work");

        GraGra graGra = new GraGra();
        assertNotNull(XMLAdapterFactory.createAdapter(graGra), "Should create adapter for GraGra");
    }
}
