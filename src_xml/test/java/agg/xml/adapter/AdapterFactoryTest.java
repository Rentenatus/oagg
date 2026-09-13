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
import agg.xt_basis.TypeGraph;
import agg.xt_basis.TypeSet;
import agg.xt_basis.Rule;
import agg.xt_basis.agt.RuleScheme;
import agg.xt_basis.agt.MultiRule;
import agg.parser.ExcludePairContainer;
import agg.parser.DependencyPairContainer;
import agg.parser.LayeredExcludePairContainer;
import agg.parser.LayeredDependencyPairContainer;
import agg.parser.PriorityExcludePairContainer;
import agg.parser.PriorityDependencyPairContainer;
import agg.parser.ConflictsDependenciesContainer;
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

    // --- Subtype dispatch tests (AP-3: instanceof ordering fix) ---

    @Test
    public void testTypeGraphGetsTypeGraphAdapter() {
        GraGra graGra = new GraGra();
        TypeSet typeSet = graGra.getTypeSet();
        TypeGraph typeGraph = new TypeGraph(typeSet);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(typeGraph);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof TypeGraphAdapter,
            "TypeGraph should get TypeGraphAdapter, not GraphAdapter");
        assertFalse(adapter instanceof GraphAdapter,
            "TypeGraph should NOT get the generic GraphAdapter");
    }

    @Test
    public void testRuleSchemeGetsRuleSchemeAdapter() {
        GraGra graGra = new GraGra();
        TypeSet typeSet = graGra.getTypeSet();
        RuleScheme ruleScheme = new RuleScheme("testScheme", typeSet);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(ruleScheme);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof AgtAdapters.RuleSchemeAdapter,
            "RuleScheme should get RuleSchemeAdapter, not RuleAdapter");
        assertFalse(adapter instanceof RuleAdapter,
            "RuleScheme should NOT get the generic RuleAdapter");
    }

    @Test
    public void testMultiRuleGetsMultiRuleAdapter() {
        GraGra graGra = new GraGra();
        TypeSet typeSet = graGra.getTypeSet();
        MultiRule multiRule = new MultiRule(typeSet);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(multiRule);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof AgtAdapters.MultiRuleAdapter,
            "MultiRule should get MultiRuleAdapter, not RuleAdapter");
        assertFalse(adapter instanceof RuleAdapter,
            "MultiRule should NOT get the generic RuleAdapter");
    }

    @Test
    public void testExcludePairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        ExcludePairContainer container = new ExcludePairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.ExcludePairContainerAdapter,
            "ExcludePairContainer should get ExcludePairContainerAdapter");
    }

    @Test
    public void testDependencyPairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        DependencyPairContainer container = new DependencyPairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.DependencyPairContainerAdapter,
            "DependencyPairContainer should get DependencyPairContainerAdapter, not ExcludePairContainerAdapter");
        assertFalse(adapter instanceof ParserAdapters.ExcludePairContainerAdapter,
            "DependencyPairContainer should NOT get the generic ExcludePairContainerAdapter");
    }

    @Test
    public void testLayeredExcludePairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        LayeredExcludePairContainer container = new LayeredExcludePairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.LayeredExcludePairContainerAdapter,
            "LayeredExcludePairContainer should get LayeredExcludePairContainerAdapter");
    }

    @Test
    public void testLayeredDependencyPairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        LayeredDependencyPairContainer container = new LayeredDependencyPairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.LayeredDependencyPairContainerAdapter,
            "LayeredDependencyPairContainer should get LayeredDependencyPairContainerAdapter");
    }

    @Test
    public void testPriorityExcludePairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        PriorityExcludePairContainer container = new PriorityExcludePairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.PriorityExcludePairContainerAdapter,
            "PriorityExcludePairContainer should get PriorityExcludePairContainerAdapter");
    }

    @Test
    public void testPriorityDependencyPairContainerGetsCorrectAdapter() {
        GraGra graGra = new GraGra();
        PriorityDependencyPairContainer container = new PriorityDependencyPairContainer(graGra);
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.PriorityDependencyPairContainerAdapter,
            "PriorityDependencyPairContainer should get PriorityDependencyPairContainerAdapter");
    }

    @Test
    public void testConflictsDependenciesContainerGetsCorrectAdapter() {
        ConflictsDependenciesContainer container = new ConflictsDependenciesContainer();
        XMLSerializable adapter = XMLAdapterFactory.createAdapter(container);
        assertNotNull(adapter, "Adapter should not be null");
        assertTrue(adapter instanceof ParserAdapters.ConflictsDependenciesContainerAdapter,
            "ConflictsDependenciesContainer should get ConflictsDependenciesContainerAdapter");
    }
}
