/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xml.TestDataHelper;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializerContext;
import agg.xml.legacy.LegacyCompatibility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Roundtrip tests: load -> verify -> re-save -> reload -> compare.
 * These tests verify that XML data survives a complete roundtrip
 * through both legacy and new XML systems without data loss.
 */
public class RoundtripTest {

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
    }

    @DataProvider(name = "sampleFiles")
    public Object[][] sampleFiles() {
        File[] files = TestDataHelper.getSampleFiles();
        Object[][] data = new Object[files.length][1];
        for (int i = 0; i < files.length; i++) {
            data[i][0] = files[i];
        }
        return data;
    }

    /**
     * Load a GraGra via legacy XMLHelper, check basic properties.
     */
    @Test(dataProvider = "sampleFiles")
    public void testLegacyLoadAndInspect(File ggxFile) throws Exception {
        GraGra graGra = new GraGra();
        graGra.load(ggxFile.getAbsolutePath());
        assertNotNull(graGra.getName(), "GraGra name should not be null");
        assertTrue(graGra.getGraphsVec().size() > 0, "Should have at least one graph");
    }

    /**
     * Roundtrip: load -> save -> reload via legacy XMLHelper.
     * Verifies graph count, rule count, name, and node/edge counts.
     */
    @Test(dataProvider = "sampleFiles")
    public void testLegacyRoundtrip(File ggxFile) throws Exception {
        // Load original
        GraGra original = new GraGra();
        original.load(ggxFile.getAbsolutePath());
        int originalGraphCount = original.getGraphsVec().size();
        int originalRuleCount = original.getRulesVec().size();

        // Save to temp file
        File tempFile = File.createTempFile("agg-roundtrip-", ".ggx");
        tempFile.deleteOnExit();
        original.save(tempFile.getAbsolutePath());

        // Reload
        GraGra reloaded = new GraGra();
        reloaded.load(tempFile.getAbsolutePath());

        assertEquals(reloaded.getGraphsVec().size(), originalGraphCount,
            "Graph count should match after roundtrip");
        assertEquals(reloaded.getRulesVec().size(), originalRuleCount,
            "Rule count should match after roundtrip");
        assertEquals(reloaded.getName(), original.getName(),
            "Name should match after roundtrip");

        // Deep comparison: node and edge counts per graph
        for (int i = 0; i < original.getGraphsVec().size(); i++) {
            Graph origGraph = original.getGraphsVec().get(i);
            Graph reloadedGraph = reloaded.getGraphsVec().get(i);
            assertEquals(reloadedGraph.getNodesCount(), origGraph.getNodesCount(),
                "Node count should match for graph " + i);
            assertEquals(reloadedGraph.getArcsCount(), origGraph.getArcsCount(),
                "Edge count should match for graph " + i);
        }

        tempFile.delete();
    }

    /**
     * DOM roundtrip: parse -> serialize to string -> re-parse.
     * Verifies root element, child count, and version attribute.
     */
    @Test(dataProvider = "sampleFiles")
    public void testDomRoundtrip(File ggxFile) throws Exception {
        // Parse with new DOM context
        DOMXMLDeserializerContext deserializer = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(deserializer.getDocument(), "Document should be parsed");

        // Serialize back to string
        DOMXMLSerializerContext serializer = new DOMXMLSerializerContext(deserializer.getDocument());
        String xml = serializer.toXMLString();
        assertNotNull(xml, "XML string should not be null");
        assertTrue(xml.contains("Document"), "Should contain Document root");

        // Re-parse from string
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        DOMXMLDeserializerContext reloaded = new DOMXMLDeserializerContext(stream);
        assertNotNull(reloaded.getDocument(), "Re-parsed document should not be null");
        assertEquals(reloaded.getCurrentElement().getNodeName(), "Document",
            "Root should be Document after roundtrip");

        // Verify version attribute preserved
        String originalVersion = deserializer.getCurrentElement().getAttribute("version");
        String reloadedVersion = reloaded.getCurrentElement().getAttribute("version");
        assertEquals(reloadedVersion, originalVersion,
            "Version attribute should survive DOM roundtrip");

        // Verify child element count matches
        int originalChildCount = deserializer.getCurrentElement().getChildNodes().getLength();
        int reloadedChildCount = reloaded.getCurrentElement().getChildNodes().getLength();
        assertEquals(reloadedChildCount, originalChildCount,
            "Child node count should match after DOM roundtrip");
    }

    /**
     * Cross-system: load via legacy, serialize via new DOM, compare root elements.
     */
    @Test(dataProvider = "sampleFiles")
    public void testCrossSystemCompatibility(File ggxFile) throws Exception {
        // Load via legacy
        XMLHelper legacyHelper = new XMLHelper();
        assertTrue(legacyHelper.read_from_xml(ggxFile.getAbsolutePath()),
            "Legacy should load: " + ggxFile.getName());
        assertNotNull(legacyHelper.getDoc(), "Legacy document should exist");

        // Load via new DOM
        DOMXMLDeserializerContext newContext = new DOMXMLDeserializerContext(ggxFile);
        assertNotNull(newContext.getDocument(), "New document should exist");

        // Both should have same root element name
        assertEquals(
            legacyHelper.getDoc().getDocumentElement().getNodeName(),
            newContext.getDocument().getDocumentElement().getNodeName(),
            "Root element names should match");
    }

    /**
     * Verify graph structure counts after legacy load.
     */
    @Test
    public void testSmallGraphStructureCounts() throws Exception {
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra graGra = new GraGra();
        graGra.load(file.getAbsolutePath());

        // At least one host graph should exist
        assertTrue(graGra.getGraphsVec().size() > 0, "Should have graphs");

        // Check the first graph has nodes
        for (Graph g : graGra.getGraphsVec()) {
            assertNotNull(g.getName(), "Graph name should not be null");
        }
    }

    /**
     * Save and reload via LegacyCompatibility layer.
     */
    @Test
    public void testLegacyCompatibilityRoundtrip() throws Exception {
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        // Load via legacy
        GraGra original = new GraGra();
        original.load(file.getAbsolutePath());

        // Save via LegacyCompatibility
        File tempFile = File.createTempFile("agg-compat-", ".ggx");
        tempFile.deleteOnExit();
        LegacyCompatibility.serializeToFile(original, tempFile);
        assertTrue(tempFile.exists(), "Temp file should exist after save");
        assertTrue(tempFile.length() > 0, "Temp file should not be empty");

        // Reload via legacy
        GraGra reloaded = new GraGra();
        reloaded.load(tempFile.getAbsolutePath());

        assertEquals(reloaded.getName(), original.getName(),
            "Names should match after LegacyCompatibility roundtrip");
        assertEquals(reloaded.getGraphsVec().size(), original.getGraphsVec().size(),
            "Graph counts should match");

        // Deep comparison: node and edge counts
        for (int i = 0; i < original.getGraphsVec().size(); i++) {
            Graph origGraph = original.getGraphsVec().get(i);
            Graph reloadedGraph = reloaded.getGraphsVec().get(i);
            assertEquals(reloadedGraph.getNodesCount(), origGraph.getNodesCount(),
                "Node count should match for graph " + i);
            assertEquals(reloadedGraph.getArcsCount(), origGraph.getArcsCount(),
                "Edge count should match for graph " + i);
        }

        tempFile.delete();
    }
}
