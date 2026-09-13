/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializerContext;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Stress tests verifying that the XML system handles large files
 * and repeated operations without failures.
 */
public class StressTest {

    private static final String BASELINE_DIR = "../test_xml/baseline/samples/";
    private static final int REPEAT_COUNT = 10;

    @Test
    public void testRepeatedLegacyLoad() throws Exception {
        File file = new File(BASELINE_DIR + "large_graph.ggx");
        if (!file.exists()) return;

        for (int i = 0; i < REPEAT_COUNT; i++) {
            XMLHelper helper = new XMLHelper();
            boolean loaded = helper.read_from_xml(file.getAbsolutePath());
            assertTrue(loaded, "Load should succeed on iteration " + i);
            assertNotNull(helper.getDoc(), "Document should exist on iteration " + i);
        }
    }

    @Test
    public void testRepeatedDomLoad() throws Exception {
        File file = new File(BASELINE_DIR + "large_graph.ggx");
        if (!file.exists()) return;

        for (int i = 0; i < REPEAT_COUNT; i++) {
            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            assertNotNull(context.getDocument(), "Document should exist on iteration " + i);
        }
    }

    @Test
    public void testRepeatedGraGraLoad() throws Exception {
        File file = new File(BASELINE_DIR + "small_graph.ggx");
        if (!file.exists()) return;

        for (int i = 0; i < REPEAT_COUNT; i++) {
            GraGra graGra = new GraGra();
            graGra.load(file.getAbsolutePath());
            assertNotNull(graGra.getName(), "Name should not be null on iteration " + i);
        }
    }

    @Test
    public void testRepeatedDomRoundtrip() throws Exception {
        File file = new File(BASELINE_DIR + "medium_graph.ggx");
        if (!file.exists()) return;

        for (int i = 0; i < REPEAT_COUNT; i++) {
            DOMXMLDeserializerContext deserializer = new DOMXMLDeserializerContext(file);
            DOMXMLSerializerContext serializer = new DOMXMLSerializerContext(deserializer.getDocument());
            String xml = serializer.toXMLString();
            assertNotNull(xml, "XML should not be null on iteration " + i);

            ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            DOMXMLDeserializerContext reloaded = new DOMXMLDeserializerContext(stream);
            assertNotNull(reloaded.getDocument(), "Reloaded doc should exist on iteration " + i);
        }
    }

    @Test
    public void testLargeFileLoadPerformance() throws Exception {
        File file = new File(BASELINE_DIR + "large_graph.ggx");
        if (!file.exists()) return;

        long start = System.currentTimeMillis();
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(context.getDocument(), "Large file should load");
        assertTrue(elapsed < 5000, "Large file should load in under 5s, took " + elapsed + "ms");
    }

    @Test
    public void testMemoryStability() throws Exception {
        File file = new File(BASELINE_DIR + "small_graph.ggx");
        if (!file.exists()) return;

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < 20; i++) {
            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
            assertNotNull(context.getDocument(), "Document should exist on iteration " + i);
            context = null; // Allow GC
        }

        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryDelta = finalMemory - initialMemory;

        // Memory delta should be reasonable (not a hard leak)
        assertTrue(memoryDelta < 50_000_000,
            "Memory delta should be < 50MB, was " + (memoryDelta / 1024) + "KB");
    }

    @Test
    public void testAllFilesLoadUnderBothSystems() throws Exception {
        String[] files = {"small_graph.ggx", "small_graph_layered.ggx",
                          "medium_graph.ggx", "large_graph.ggx"};

        for (String filename : files) {
            File file = new File(BASELINE_DIR + filename);
            if (!file.exists()) continue;

            // Legacy
            XMLHelper legacy = new XMLHelper();
            assertTrue(legacy.read_from_xml(file.getAbsolutePath()),
                "Legacy should load: " + filename);

            // New DOM
            DOMXMLDeserializerContext dom = new DOMXMLDeserializerContext(file);
            assertNotNull(dom.getDocument(), "DOM should load: " + filename);

            // GraGra
            GraGra graGra = new GraGra();
            graGra.load(file.getAbsolutePath());
            assertNotNull(graGra.getName(), "GraGra should load: " + filename);
        }
    }
}
