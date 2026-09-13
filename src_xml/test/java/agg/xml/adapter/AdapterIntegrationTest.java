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
import agg.xt_basis.GraGra;
import agg.xt_basis.Node;
import agg.xt_basis.Rule;
import agg.xml.TestDataHelper;
import agg.xml.core.XMLSerializable;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializerContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Integration tests for the adapter pattern implementation.
 * Tests that adapters can correctly wrap legacy XMLObject implementations
 * and work with the new XML serialization infrastructure.
 */
public class AdapterIntegrationTest {

    private XMLHelper xmlHelper;

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
        xmlHelper = new XMLHelper();
    }

    /**
     * Test that XMLObjectAdapter can wrap a Graph object
     */
    @Test
    public void testGraphAdapterCreation() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
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
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        XMLHelper helper = new XMLHelper();
        helper.read_from_xml(ggxFile.getAbsolutePath());

        // Create a Graph and wrap it
        Graph graph = new Graph();
        XMLSerializable graphAdapter = XMLAdapterFactory.createAdapter(graph);
        assertNotNull(graphAdapter, "Should create adapter for Graph");
        assertTrue(graphAdapter instanceof GraphAdapter, "Should create GraphAdapter");

        // Create a Node and wrap it (using null for required parameters)
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
    public void testAdapterImplementsXMLObject() {
        Graph graph = new Graph();
        GraphAdapter adapter = new GraphAdapter(graph);

        assertTrue(adapter.getWrappedObject() instanceof XMLObject,
            "Wrapped object should be XMLObject");
        assertTrue(adapter instanceof XMLSerializable,
            "Adapter should implement XMLSerializable");
    }

    /**
     * Test adapter with XMLHelperSerializerContext
     */
    @Test
    public void testAdapterWithXMLHelperContext() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        XMLHelper helper = new XMLHelper();
        helper.read_from_xml(ggxFile.getAbsolutePath());

        XMLHelperSerializerContext context = new XMLHelperSerializerContext(helper);
        assertNotNull(context, "Context should be created");

        assertEquals(context.getXMLHelper(), helper, "Should return the same XMLHelper");
    }

    /**
     * Test that we can use both legacy and new approaches on the same data
     */
    @Test
    public void testDualApproachCompatibility() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");

        // Approach 1: Legacy XMLHelper
        XMLHelper legacyHelper = new XMLHelper();
        boolean legacyLoaded = legacyHelper.read_from_xml(ggxFile.getAbsolutePath());
        assertTrue(legacyLoaded, "Legacy approach should work");

        // Approach 2: New DOM-based approach
        DOMXMLDeserializerContext newContext = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(newContext, "New approach should work");

        assertNotNull(legacyHelper.getDoc(), "Legacy should have document");
        assertNotNull(newContext.getDocument(), "New should have document");
    }

    /**
     * Test that RuleAdapter.serializeToElement produces a Rule DOM element
     * with correct attributes and child structure.
     */
    @Test
    public void testRuleAdapterSerializeToElement() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        GraGra graGra = new GraGra();
        graGra.load(ggxFile.getAbsolutePath());

        List<Rule> rules = graGra.getRulesVec();
        assertTrue(rules.size() > 0, "Should have rules");

        DOMSerializationRegistry registry = new DOMSerializationRegistry();
        Document doc = new DOMXMLSerializerContext().getDocument();

        for (Rule rule : rules) {
            RuleAdapter ruleAdapter = new RuleAdapter(rule);
            Element ruleElem = ruleAdapter.serializeToElement(doc, registry);
            assertNotNull(ruleElem, "Rule element should not be null");
            assertEquals(ruleElem.getTagName(), "Rule", "Tag should be Rule");
            assertTrue(ruleElem.hasAttribute("ID"), "Should have ID attribute");
            assertTrue(ruleElem.hasAttribute("name"), "Should have name attribute");
            assertEquals(ruleElem.getAttribute("name"), rule.getName(),
                "Name should match");

            // Should have at least LHS and RHS graph children
            int graphCount = countChildElements(ruleElem, "Graph");
            assertTrue(graphCount >= 2, "Rule should have at least 2 Graph children (LHS+RHS)");

            // Should have Morphism child
            int morphismCount = countChildElements(ruleElem, "Morphism");
            assertTrue(morphismCount >= 1, "Rule should have a Morphism child");

            // Should have TaggedValue children for layer and priority
            int taggedValueCount = countChildElements(ruleElem, "TaggedValue");
            assertTrue(taggedValueCount >= 2, "Rule should have layer and priority TaggedValues");
        }
    }

    /**
     * Test that GraphAdapter.serializeToElement produces a Graph DOM element
     * with correct node and edge children.
     */
    @Test
    public void testGraphAdapterSerializeToElement() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        GraGra graGra = new GraGra();
        graGra.load(ggxFile.getAbsolutePath());

        assertTrue(graGra.getGraphsVec().size() > 0, "Should have graphs");

        DOMSerializationRegistry registry = new DOMSerializationRegistry();
        Document doc = new DOMXMLSerializerContext().getDocument();

        for (Graph graph : graGra.getGraphsVec()) {
            GraphAdapter graphAdapter = new GraphAdapter(graph);
            Element graphElem = graphAdapter.serializeToElement(doc, registry);
            assertNotNull(graphElem, "Graph element should not be null");
            assertEquals(graphElem.getTagName(), "Graph", "Tag should be Graph");
            assertTrue(graphElem.hasAttribute("ID"), "Should have ID attribute");

            // Should have Node and Edge children
            int nodeCount = countChildElements(graphElem, "Node");
            int edgeCount = countChildElements(graphElem, "Edge");
            assertEquals(nodeCount, graph.getNodesCount(),
                "Node count should match");
            assertEquals(edgeCount, graph.getArcsCount(),
                "Edge count should match");
        }
    }

    /**
     * Test that FormulaAdapter and AtomConstraintAdapter serialize to elements.
     */
    @Test
    public void testConstraintAdaptersSerializeToElement() throws Exception {
        File ggxFile = TestDataHelper.resolveSample("small_graph.ggx");
        GraGra graGra = new GraGra();
        graGra.load(ggxFile.getAbsolutePath());

        DOMSerializationRegistry registry = new DOMSerializationRegistry();
        Document doc = new DOMXMLSerializerContext().getDocument();

        // Test Formula serialization
        List<agg.cons.Formula> formulas = graGra.getConstraintsVec();
        for (agg.cons.Formula formula : formulas) {
            FormulaAdapter formulaAdapter = new FormulaAdapter(formula);
            Element formulaElem = formulaAdapter.serializeToElement(doc, registry);
            assertNotNull(formulaElem, "Formula element should not be null");
            assertEquals(formulaElem.getTagName(), "Formula",
                "Tag should be Formula");
            assertTrue(formulaElem.hasAttribute("name"),
                "Should have name attribute");
        }

        // Test AtomConstraint serialization
        java.util.Enumeration<agg.cons.AtomConstraint> atomics = graGra.getAtomics();
        while (atomics.hasMoreElements()) {
            agg.cons.AtomConstraint atom = atomics.nextElement();
            AtomConstraintAdapter atomAdapter = new AtomConstraintAdapter(atom);
            Element atomElem = atomAdapter.serializeToElement(doc, registry);
            assertNotNull(atomElem, "AtomConstraint element should not be null");
            assertEquals(atomElem.getTagName(), "Graphconstraint_Atomic",
                "Tag should be Graphconstraint_Atomic");
            assertTrue(atomElem.hasAttribute("name"),
                "Should have name attribute");
        }
    }

    private static int countChildElements(Element parent, String tagName) {
        int count = 0;
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && tagName.equals(child.getNodeName())) {
                count++;
            }
        }
        return count;
    }
}
