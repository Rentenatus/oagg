/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml;

import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Tests for the central {@link XMLSerialization} utility class.
 */
public class XMLSerializationTest {

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
    }

    @AfterTest
    public void tearDown() {
        XMLSerialization.setUseNewXml(false);
    }

    @Test
    public void testFeatureFlagToggle() {
        XMLSerialization.setUseNewXml(false);
        assertFalse(XMLSerialization.isUseNewXml(), "Should be false by default");
        XMLSerialization.setUseNewXml(true);
        assertTrue(XMLSerialization.isUseNewXml(), "Should be true after set");
        XMLSerialization.setUseNewXml(false);
        assertFalse(XMLSerialization.isUseNewXml(), "Should be false after reset");
    }

    @Test
    public void testSaveAndLoadWithLegacy() throws Exception {
        XMLSerialization.setUseNewXml(false);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra original = new GraGra();
        XMLSerialization.load(original, file.getAbsolutePath());
        assertNotNull(original.getName(), "Should load via legacy");

        File tempFile = new File("target/xmlserialization-test-legacy.ggx");
        tempFile.getParentFile().mkdirs();
        assertTrue(XMLSerialization.save(original, tempFile.getAbsolutePath()),
            "Save should succeed");
        assertTrue(tempFile.exists(), "File should exist");
        assertTrue(tempFile.length() > 0, "File should not be empty");

        GraGra reloaded = new GraGra();
        XMLSerialization.load(reloaded, tempFile.getAbsolutePath());
        assertEquals(reloaded.getName(), original.getName(),
            "Names should match after roundtrip");
        assertEquals(reloaded.getGraphsVec().size(), original.getGraphsVec().size(),
            "Graph counts should match");

        tempFile.delete();
    }

    @Test
    public void testSaveAndLoadWithDom() throws Exception {
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra original = new GraGra();
        XMLSerialization.loadWithDom(original, file.getAbsolutePath());
        assertNotNull(original.getName(), "Should load via DOM");

        File tempFile = new File("target/xmlserialization-test-dom.ggx");
        tempFile.getParentFile().mkdirs();
        assertTrue(XMLSerialization.saveWithDom(original, tempFile.getAbsolutePath()),
            "Save with DOM should succeed");
        assertTrue(tempFile.exists(), "File should exist");
        assertTrue(tempFile.length() > 0, "File should not be empty");

        GraGra reloaded = new GraGra();
        XMLSerialization.loadWithDom(reloaded, tempFile.getAbsolutePath());
        assertEquals(reloaded.getName(), original.getName(),
            "Names should match after DOM roundtrip");
        assertEquals(reloaded.getGraphsVec().size(), original.getGraphsVec().size(),
            "Graph counts should match after DOM roundtrip");

        // Deep comparison: node/edge counts
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

    @Test
    public void testCanLoad() {
        File file = TestDataHelper.resolveSample("small_graph.ggx");
        assertTrue(XMLSerialization.canLoad(file.getAbsolutePath()),
            "Should be able to load valid .ggx file");
        assertFalse(XMLSerialization.canLoad("nonexistent.ggx"),
            "Should not be able to load nonexistent file");
        assertFalse(XMLSerialization.canLoad("test.txt"),
            "Should not be able to load non-.ggx file");
    }

    @Test
    public void testValidate() {
        File file = TestDataHelper.resolveSample("small_graph.ggx");
        assertTrue(XMLSerialization.validate(file.getAbsolutePath()),
            "Valid .ggx file should pass validation");
    }

    @Test
    public void testSaveToString() throws Exception {
        File file = TestDataHelper.resolveSample("small_graph.ggx");
        GraGra graGra = new GraGra();
        graGra.load(file.getAbsolutePath());

        String xml = XMLSerialization.saveToString(graGra);
        assertNotNull(xml, "XML string should not be null");
        assertTrue(xml.contains("GraphTransformationSystem"),
            "XML should contain GraphTransformationSystem");
        assertTrue(xml.contains("Document"),
            "XML should contain Document root");
    }

    @Test
    public void testSaveAndLoadViaFeatureFlag() throws Exception {
        XMLSerialization.setUseNewXml(true);
        File file = TestDataHelper.resolveSample("small_graph.ggx");

        GraGra original = new GraGra();
        XMLSerialization.load(original, file.getAbsolutePath());
        assertNotNull(original.getName(), "Should load via new XML path");

        File tempFile = new File("target/xmlserialization-test-flag.ggx");
        tempFile.getParentFile().mkdirs();
        assertTrue(XMLSerialization.save(original, tempFile.getAbsolutePath()),
            "Save via feature flag should succeed");

        GraGra reloaded = new GraGra();
        XMLSerialization.load(reloaded, tempFile.getAbsolutePath());
        assertEquals(reloaded.getName(), original.getName(),
            "Names should match after feature-flag roundtrip");

        tempFile.delete();
    }
}
