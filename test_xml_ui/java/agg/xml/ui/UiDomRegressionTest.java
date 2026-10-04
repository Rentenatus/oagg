/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdArc;
import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdGraph;
import agg.editor.impl.EdNode;
import agg.layout.evolutionary.LayoutArc;
import agg.layout.evolutionary.LayoutNode;
import agg.xml.validation.XmlCanonicalComparator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * DOM-side regression tests of the UI layer serialization.
 *
 * <p>The preparation half ran against the FROZEN legacy clone
 * (agg-core-legacy): {@code agg.xml.prep.LegacyPreparationTest}
 * produced the reference file {@code target/legacy_prep/ui_basic_graph_attrs.ggx}
 * with the legacy XMLHelper path (EdGraGra as top object, exactly like
 * GraGraSave). This class is the "actual" half running against the current
 * src_ui / src_uixml code:</p>
 * <ol>
 *   <li>DOM load of the frozen UI reference via {@link UISerialization}.</li>
 *   <li>Verification that the UI segments (positions, text offsets, layout
 *       data) were applied onto the editor objects.</li>
 *   <li>DOM save and canonical comparison against the frozen reference.</li>
 * </ol>
 */
public class UiDomRegressionTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/ui-regression/";

    private File outputDir;

    @BeforeClass
    public void setUp() {
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * Basic graph scenario: DOM load of the frozen UI reference, UI state
     * verification, DOM save and canonical comparison.
     */
    @Test
    public void uiBasicScenarioRoundtrip() throws Exception {
        File refFile = new File(PREP_DIR, "ui_basic_graph_attrs.ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen UI reference missing: run LegacyPreparationTest first ("
            + refFile.getPath() + ")");

        EdGraGra loaded = UISerialization.loadWithDom(refFile.getPath());
        assertNotNull(loaded, "DOM UI load should return an EdGraGra");

        verifyHostGraphUiState(loaded);
        verifyTypeGraphUiState(loaded);

        File outFile = new File(outputDir, "ui_basic_graph_attrs_new.ggx");
        assertTrue(UISerialization.saveWithDom(loaded, outFile.getPath()),
            "DOM UI save should succeed");

        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "DOM UI save differs from the frozen reference: "
            + result.getMessage());
    }

    /**
     * The host graph editor objects must carry the pinned UI state from the
     * reference (distinct positions and text offset, not the EdNode/EdArc
     * defaults).
     */
    private void verifyHostGraphUiState(EdGraGra edGraGra) {
        EdGraph host = edGraGra.getGraph();
        assertNotNull(host, "Host graph should exist");
        assertEquals(host.getNodes().size(), 2, "Host graph node count");
        assertEquals(host.getArcs().size(), 1, "Host graph arc count");

        int pos = 10;
        for (EdNode node : host.getNodes()) {
            assertEquals(node.getX(), 100 + pos, "Host node X position");
            assertEquals(node.getY(), 200 + pos, "Host node Y position");
            pos += 5;

            LayoutNode layoutNode = node.getLayoutNode();
            assertNotNull(layoutNode, "Host node should have a LayoutNode");
            assertEquals(layoutNode.getAge(), 0, "LayoutNode age");
            assertEquals(layoutNode.getForce(), 10, "LayoutNode force");
            assertFalse(layoutNode.isFrozen(), "LayoutNode frozen");
            assertEquals(layoutNode.getZone(), 50, "LayoutNode zone");
        }

        EdArc arc = host.getArcs().get(0);
        assertEquals(arc.getTextOffset().x, 13, "Host arc text offset X");
        assertEquals(arc.getTextOffset().y, -7, "Host arc text offset Y");
        assertTrue(arc.hasDefaultAnchor(), "Host arc should have default anchor");

        LayoutArc layoutArc = arc.getLayoutArc();
        assertNotNull(layoutArc, "Host arc should have a LayoutArc");
        assertEquals(layoutArc.getPrefLength(), 200, "LayoutArc preflength");
        assertEquals(layoutArc.getAktLength(), 200, "LayoutArc aktlength");
        assertEquals(layoutArc.getForce(), 10, "LayoutArc force");
    }

    /**
     * The type graph editor nodes keep their default positions in the
     * reference; they must be present and carry layout nodes.
     */
    private void verifyTypeGraphUiState(EdGraGra edGraGra) {
        EdGraph typeGraph = edGraGra.getTypeGraph();
        assertNotNull(typeGraph, "Type graph should exist");
        assertTrue(typeGraph.getNodes().size() >= 2, "Type graph node count");

        for (EdNode node : typeGraph.getNodes()) {
            assertEquals(node.getX(), 100, "Type graph node X position");
            assertEquals(node.getY(), 100, "Type graph node Y position");
            assertNotNull(node.getLayoutNode(),
                "Type graph node should have a LayoutNode");
        }
        for (EdArc arc : typeGraph.getArcs()) {
            assertTrue(arc.isElementOfTypeGraph(),
                "Type graph arcs are type graph elements");
        }
    }
}
