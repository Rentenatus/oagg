/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xml.XMLSerialization;
import agg.xt_basis.GraGra;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Node;
import agg.xt_basis.Rule;
import agg.xml.validation.TestDataGenerator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;
import java.util.Iterator;

/**
 * Morphism serialization matrix: rules are morphisms (LHS -> RHS), so every
 * combination of LHS structure, RHS structure and mapped image is covered.
 *
 * <p>Dimensions:
 * <ul>
 *   <li>LHS: {@code nothing | nodes only | nodes+edges}</li>
 *   <li>RHS: {@code nothing | nodes only | nodes+edges}</li>
 *   <li>Mapping: {@code nothing mapped | only nodes mapped | nodes+edges mapped}</li>
 *   <li>Graph orientation: {@code directed | undirected}</li>
 *   <li>Type graph: {@code with | without}</li>
 * </ul>
 *
 * <p>Only structurally sensible combinations are included: a node mapping
 * requires nodes on both sides, an edge mapping requires edges on both sides
 * (and mapped endpoints). This yields 14 combinations per orientation and
 * type graph setting, 56 test invocations in total.</p>
 *
 * <p>The scenario generation and the legacy save ran in the PREPARATION
 * phase against the FROZEN legacy clone (agg-core-legacy), producing
 * {@code target/legacy_prep/morph_<...>.ggx}. This class is the "actual"
 * half running against the CURRENT code in src: DOM load of the frozen
 * reference, structural verification of the morphism domain, DOM save
 * and canonical comparison.</p>
 */
public class MorphismMatrixTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/morphism-matrix/";

    private File outputDir;

    @BeforeClass
    public void setUp() {
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        XMLSerialization.setUseNewXml(false);
    }

    @AfterClass
    public void tearDown() {
        XMLSerialization.setUseNewXml(false);
    }

    @DataProvider(name = "morphismMatrix")
    public Object[][] morphismMatrix() {
        // Structurally sensible LHS/RHS/mapping combinations
        int[][] combinations = new int[][] {
            // LHS nothing: mapping must stay empty, RHS free
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            // LHS nodes only: mapping empty or nodes only
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES},
            // LHS nodes+edges: mapping empty, nodes only, or nodes+edges
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES_EDGES},
        };
        boolean[] orientations = {true, false};
        boolean[] typeGraphs = {true, false};
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (int[] combo : combinations) {
            for (boolean directed : orientations) {
                for (boolean withTypeGraph : typeGraphs) {
                    rows.add(new Object[]{combo[0], combo[1], combo[2], directed, withTypeGraph});
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }

    /**
     * Runs the DOM roundtrip battery for one matrix cell against the
     * frozen legacy reference.
     */
    @Test(dataProvider = "morphismMatrix")
    public void testRuleMorphismRoundtrip(int lhsSpec, int rhsSpec, int mapSpec, boolean directed,
            boolean withTypeGraph) throws Exception {
        String baseName = prepBaseName(lhsSpec, rhsSpec, mapSpec, directed, withTypeGraph);
        File refFile = prepFile(baseName);

        // Expected mapping counts
        int expectedNodeMappings = (mapSpec >= TestDataGenerator.MAP_NODES) ? 2 : 0;
        int expectedArcMappings = (mapSpec == TestDataGenerator.MAP_NODES_EDGES) ? 1 : 0;

        // Step 1: Load frozen reference with new DOM path
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, refFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load of the frozen reference should produce a named GraGra: " + baseName);

        // Step 2: Verify structure and morphism domain after DOM load
        verifyRoundtripStructure(graGraFromOld, lhsSpec, rhsSpec, directed, withTypeGraph,
            expectedNodeMappings, expectedArcMappings, baseName);

        // Step 3: Save with new DOM path
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + baseName);
        assertTrue(newFile.exists() && newFile.length() > 0,
            "New XML file should be non-empty: " + baseName);

        // Step 4: Reload the DOM save and verify again (DOM path stability)
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile.getAbsolutePath());
        verifyRoundtripStructure(graGraReloaded, lhsSpec, rhsSpec, directed, withTypeGraph,
            expectedNodeMappings, expectedArcMappings, baseName + "_reload");

        // Step 5: Canonical comparison against the frozen reference
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + baseName + ": " + result.getMessage());
    }

    /**
     * Verifies LHS/RHS structure, orientation, type graph presence and the
     * morphism domain of the single rule after a load.
     */
    private void verifyRoundtripStructure(GraGra graGra, int lhsSpec, int rhsSpec,
            boolean directed, boolean withTypeGraph, int expectedNodeMappings,
            int expectedArcMappings, String label) {

        assertEquals(graGra.getTypeSet().isArcDirected(), directed,
            "Orientation should survive the roundtrip: " + label);
        assertEquals(graGra.getTypeSet().getTypeGraph() != null, withTypeGraph,
            "Type graph presence should survive the roundtrip: " + label);

        assertEquals(graGra.getRulesVec().size(), 1,
            "Exactly one rule expected: " + label);
        Rule rule = graGra.getRulesVec().get(0);

        int expectedLhsNodes = (lhsSpec >= TestDataGenerator.STRUCT_NODES) ? 2 : 0;
        int expectedLhsArcs = (lhsSpec == TestDataGenerator.STRUCT_NODES_EDGES) ? 1 : 0;
        int expectedRhsNodes = (rhsSpec >= TestDataGenerator.STRUCT_NODES) ? 2 : 0;
        int expectedRhsArcs = (rhsSpec == TestDataGenerator.STRUCT_NODES_EDGES) ? 1 : 0;

        assertEquals(rule.getLeft().getNodesCount(), expectedLhsNodes,
            "LHS node count should match: " + label);
        assertEquals(rule.getLeft().getArcsCount(), expectedLhsArcs,
            "LHS edge count should match: " + label);
        assertEquals(rule.getTarget().getNodesCount(), expectedRhsNodes,
            "RHS node count should match: " + label);
        assertEquals(rule.getTarget().getArcsCount(), expectedRhsArcs,
            "RHS edge count should match: " + label);

        // Morphism domain: count mapped nodes and arcs
        int mappedNodes = 0;
        int mappedArcs = 0;
        Iterator<GraphObject> domain = rule.getDomain();
        while (domain.hasNext()) {
            GraphObject obj = domain.next();
            if (obj instanceof Node) {
                mappedNodes++;
            } else {
                mappedArcs++;
            }
        }
        assertEquals(mappedNodes, expectedNodeMappings,
            "Mapped node count should match: " + label);
        assertEquals(mappedArcs, expectedArcMappings,
            "Mapped edge count should match: " + label);

        // Every domain object must have a non-null image
        Iterator<GraphObject> domainImages = rule.getDomain();
        while (domainImages.hasNext()) {
            GraphObject obj = domainImages.next();
            assertNotNull(rule.getImage(obj),
                "Every mapped object must have an image: " + label);
        }
    }

    private String prepBaseName(int lhsSpec, int rhsSpec, int mapSpec, boolean directed,
            boolean withTypeGraph) {
        return "morph_" + lhsSpec + "_" + rhsSpec + "_" + mapSpec
            + (directed ? "_dir" : "_undir") + (withTypeGraph ? "_TG" : "_noTG");
    }

    private File prepFile(String baseName) {
        File refFile = new File(PREP_DIR, baseName + ".ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference file is missing (run the legacy preparation "
                + "suite first: mvn test at the parent, or -pl test/test_agg/legacy_agg): "
                + refFile.getPath());
        return refFile;
    }
}
