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
import agg.editor.impl.EdRule;
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
     * Rule scenario: rule graphs (LHS/RHS), NAC, PAC, nested AC, a bent
     * edge and a loop edge. DOM load of the frozen UI reference, UI state
     * verification, DOM save and canonical comparison.
     */
    @Test
    public void uiRuleScenarioRoundtrip() throws Exception {
        File refFile = new File(PREP_DIR, "ui_rule_nac.ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen UI reference missing: run LegacyPreparationTest first ("
            + refFile.getPath() + ")");

        EdGraGra loaded = UISerialization.loadWithDom(refFile.getPath());
        assertNotNull(loaded, "DOM UI load should return an EdGraGra");

        assertEquals(loaded.getRules().size(), 1, "Rule count");
        EdRule rule = loaded.getRules().get(0);
        assertEquals(rule.getName(), "transformItem", "Rule name");

        verifyRuleGraphUiState(rule);
        verifyHostLoopUiState(loaded);

        File outFile = new File(outputDir, "ui_rule_nac_new.ggx");
        assertTrue(UISerialization.saveWithDom(loaded, outFile.getPath()),
            "DOM UI save should succeed");

        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "DOM UI save differs from the frozen reference: "
            + result.getMessage());
    }

    /**
     * The rule graphs must carry the pinned UI state: distinct LHS/RHS/NAC/
     * PAC positions and the bent LHS edge.
     */
    private void verifyRuleGraphUiState(EdRule rule) {
        assertEquals(rule.getLeft().getNodes().size(), 2, "LHS node count");
        assertEquals(rule.getLeft().getArcs().size(), 1, "LHS arc count");
        assertEquals(rule.getRight().getNodes().size(), 1, "RHS node count");

        assertTrue(hasNodeAt(rule.getLeft(), 131, 231), "LHS node 1 position");
        assertTrue(hasNodeAt(rule.getLeft(), 138, 238), "LHS node 2 position");
        assertTrue(hasNodeAt(rule.getRight(), 145, 245), "RHS node position");

        EdArc lhsArc = rule.getLeft().getArcs().get(0);
        assertEquals(lhsArc.getX(), 25, "Bent LHS edge X");
        assertEquals(lhsArc.getY(), 30, "Bent LHS edge Y");
        assertFalse(lhsArc.hasDefaultAnchor(), "Bent LHS edge anchor");

        assertEquals(rule.getNACs().size(), 1, "NAC count");
        assertEquals(rule.getNACs().get(0).getNodes().size(), 1, "NAC node count");
        assertEquals(rule.getNACs().get(0).getNodes().get(0).getX(), 152,
            "NAC node X position");
        assertEquals(rule.getNACs().get(0).getNodes().get(0).getY(), 252,
            "NAC node Y position");

        assertEquals(rule.getPACs().size(), 1, "PAC count");
        assertEquals(rule.getPACs().get(0).getNodes().get(0).getX(), 159,
            "PAC node X position");
        assertEquals(rule.getPACs().get(0).getNodes().get(0).getY(), 259,
            "PAC node Y position");

        assertEquals(rule.getNestedACs().size(), 1, "Nested AC count");
        EdGraph nestedAC = rule.getNestedACs().get(0);
        assertTrue(hasNodeAt(nestedAC, 100, 100),
            "Nested AC node keeps the default position");
    }

    /**
     * The host graph contains a loop edge; its layout must survive the
     * roundtrip with the raw (zero) loop dimensions of the reference.
     */
    private void verifyHostLoopUiState(EdGraGra edGraGra) {
        EdGraph host = edGraGra.getGraph();
        assertEquals(host.getNodes().size(), 1, "Host node count");
        assertEquals(host.getArcs().size(), 1, "Host arc count");
        assertTrue(hasNodeAt(host, 110, 210), "Host node position");

        EdArc loop = host.getArcs().get(0);
        assertFalse(loop.isLine(), "Host edge should be a loop");
        assertEquals(loop.getWidth(), 0, "Loop width");
        assertEquals(loop.getHeight(), 0, "Loop height");
        assertTrue(loop.hasDefaultAnchor(), "Loop anchor");
    }

    /**
     * Returns whether the graph contains a node at the given position.
     */
    private boolean hasNodeAt(EdGraph graph, int x, int y) {
        for (EdNode node : graph.getNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return true;
            }
        }
        return false;
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
