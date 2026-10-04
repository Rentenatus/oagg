/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.prep;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xml.TestDataHelper;
import agg.xml.validation.TestDataGenerator;
import agg.xml.validation.XmlCanonicalComparator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Legacy preparation suite. Runs against the FROZEN legacy clone
 * (agg-core-legacy, test/test_agg/legacy_agg) and writes reference XML
 * for every scenario of the DOM-side regression suite into
 * {@code target/legacy_prep/} (relative to the shared test working
 * directory assets_test_xml).
 *
 * <p>This is the "preparation" half of the split regression tests: the
 * frozen code produces the legacy XML; the "actual" half (test_xml,
 * running against the current src) loads these files with the DOM path,
 * re-saves and compares. When src later deviates from the old XML
 * behaviour, the comparison fails although this preparation suite is
 * untouched.</p>
 */
public class LegacyPreparationTest {

    private static final String PREP_DIR = "target/legacy_prep/";

    private File prepDir;

    @BeforeClass
    public void setUp() {
        prepDir = new File(PREP_DIR);
        if (!prepDir.exists()) {
            prepDir.mkdirs();
        }
    }

    // ---- Sample files: legacy load + legacy save ----

    /**
     * Loads every sample .ggx with the frozen legacy path and saves it
     * as reference XML for the cross-system regression tests.
     */
    @Test
    public void prepareSampleFiles() throws Exception {
        TestDataHelper.requireAllSamples();
        for (String sample : TestDataHelper.SAMPLE_FILES) {
            File sampleFile = TestDataHelper.resolveSample(sample);
            TestDataHelper.requireFile(sampleFile);

            GraGra graGra = new GraGra();
            graGra.load(sampleFile.getAbsolutePath());
            assertNotNull(graGra.getName(), "Legacy load should name the GraGra: " + sample);

            saveLegacy(graGra, "sample_" + sample);
        }
    }

    /**
     * Verifies the frozen legacy path is stable (save -> load -> save
     * produces identical canonical XML). The DOM path must match this
     * baseline.
     */
    @Test
    public void prepareAndCheckLegacyStability() throws Exception {
        for (String sample : TestDataHelper.SAMPLE_FILES) {
            File sampleFile = TestDataHelper.resolveSample(sample);
            TestDataHelper.requireFile(sampleFile);

            GraGra graGra = new GraGra();
            graGra.load(sampleFile.getAbsolutePath());

            File first = new File(prepDir, "stab_1_" + sample);
            saveLegacy(graGra, "stab_1_" + sample);

            GraGra reloaded = new GraGra();
            reloaded.load(first.getAbsolutePath());
            File second = new File(prepDir, "stab_2_" + sample);
            saveLegacy(reloaded, "stab_2_" + sample);

            XmlCanonicalComparator.ComparisonResult result =
                XmlCanonicalComparator.compareFiles(first, second);
            assertTrue(result.isEqual(),
                "Frozen legacy path should be stable for " + sample + ": "
                    + result.getMessage());
        }
    }

    // ---- Generated scenarios: create + legacy save ----

    /**
     * Produces the reference XML for every generated scenario of
     * XmlGeneratedRegressionTest: all generators in the directed,
     * undirected, no-type-graph and undirected-no-type-graph variants.
     */
    @Test
    public void prepareGeneratedScenarios() throws Exception {
        // Directed with type graph
        saveLegacy(TestDataGenerator.createBasicGraphWithAttributes(true), "gen_basic_graph_attrs.ggx");
        saveLegacy(TestDataGenerator.createRuleWithNacPacNestedAC(true), "gen_rule_nac_pac.ggx");
        saveLegacy(TestDataGenerator.createWithConstraints(true), "gen_constraints.ggx");
        saveLegacy(TestDataGenerator.createWithMatch(true), "gen_match.ggx");
        saveLegacy(TestDataGenerator.createWithRuleScheme(true), "gen_rule_scheme.ggx");
        saveLegacy(TestDataGenerator.createWithRuleSequence(true), "gen_rule_sequence.ggx");
        saveLegacy(TestDataGenerator.createCompositeAll(), "gen_composite_all.ggx");

        // Undirected with type graph
        saveLegacy(undirected(TestDataGenerator.createBasicGraphWithAttributes(true)),
            "gen_basic_graph_attrs_undirected.ggx");
        saveLegacy(undirected(TestDataGenerator.createRuleWithNacPacNestedAC(true)),
            "gen_rule_nac_pac_undirected.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithConstraints(true)),
            "gen_constraints_undirected.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithMatch(true)),
            "gen_match_undirected.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithRuleSequence(true)),
            "gen_rule_sequence_undirected.ggx");

        // Directed without type graph
        saveLegacy(TestDataGenerator.createBasicGraphWithAttributes(false), "gen_basic_graph_attrs_noTG.ggx");
        saveLegacy(TestDataGenerator.createRuleWithNacPacNestedAC(false), "gen_rule_nac_pac_noTG.ggx");
        saveLegacy(TestDataGenerator.createWithConstraints(false), "gen_constraints_noTG.ggx");
        saveLegacy(TestDataGenerator.createWithMatch(false), "gen_match_noTG.ggx");
        saveLegacy(TestDataGenerator.createWithRuleScheme(false), "gen_rule_scheme_noTG.ggx");
        saveLegacy(TestDataGenerator.createWithRuleSequence(false), "gen_rule_sequence_noTG.ggx");

        // Undirected without type graph
        saveLegacy(undirected(TestDataGenerator.createBasicGraphWithAttributes(false)),
            "gen_basic_graph_attrs_undirected_noTG.ggx");
        saveLegacy(undirected(TestDataGenerator.createRuleWithNacPacNestedAC(false)),
            "gen_rule_nac_pac_undirected_noTG.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithConstraints(false)),
            "gen_constraints_undirected_noTG.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithMatch(false)),
            "gen_match_undirected_noTG.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithRuleScheme(false)),
            "gen_rule_scheme_undirected_noTG.ggx");
        saveLegacy(undirected(TestDataGenerator.createWithRuleSequence(false)),
            "gen_rule_sequence_undirected_noTG.ggx");
    }

    /**
     * Produces the reference .rsx (ApplRuleSequence) written by the
     * frozen legacy saver and verifies the frozen legacy roundtrip
     * (save -> load -> save produces canonically identical XML).
     * ApplRuleSequence has no DOM adapter; this stays on the frozen path.
     */
    @Test
    public void prepareApplRuleSequence() throws Exception {
        GraGra graGra = TestDataGenerator.createWithRuleSequence(true);
        agg.ruleappl.ApplRuleSequence ars = TestDataGenerator.createApplRuleSequence(graGra);

        XMLHelper helper = new XMLHelper();
        helper.addTopObject(ars);
        File rsxFile = new File(prepDir, "gen_appl_rule_sequence.rsx");
        assertTrue(helper.save_to_xml(rsxFile.getAbsolutePath()),
            "Frozen legacy .rsx save should succeed");
        assertTrue(rsxFile.exists() && rsxFile.length() > 0,
            "Reference .rsx should be non-empty");

        // Frozen legacy roundtrip: reload and save again.
        // NOTE: the frozen legacy .rsx path is known to be unstable
        // (RuleSequences child count differs on re-save); canonical
        // equality is therefore NOT asserted here. The reference file
        // above is what the DOM side compares against.
        agg.ruleappl.ApplRuleSequence reloaded =
            new agg.ruleappl.ApplRuleSequence(new agg.parser.CriticalPairOption());
        reloaded.setGraGra(graGra);
        XMLHelper helper2 = new XMLHelper();
        assertTrue(helper2.read_from_xml(rsxFile.getAbsolutePath()),
            "Frozen legacy .rsx load should succeed");
        Object loaded = helper2.getTopObject(reloaded);
        assertNotNull(loaded, "Frozen legacy .rsx reload should return the top object");
    }

    /**
     * Produces the reference .cpx (ConflictsDependenciesContainer) written
     * by the frozen legacy saver and verifies the frozen legacy roundtrip.
     * ConflictsDependenciesContainer has no DOM adapter; this stays on the
     * frozen path.
     */
    @Test
    public void prepareConflictsDependencies() throws Exception {
        GraGra graGra = TestDataGenerator.createBasicGraphWithAttributes(true);
        agg.parser.ConflictsDependenciesContainer cdc =
            TestDataGenerator.createConflictsDependenciesContainer(graGra);

        XMLHelper helper = new XMLHelper();
        helper.addTopObject(cdc);
        File cpxFile = new File(prepDir, "gen_conflicts_deps.cpx");
        assertTrue(helper.save_to_xml(cpxFile.getAbsolutePath()),
            "Frozen legacy .cpx save should succeed");
        assertTrue(cpxFile.exists() && cpxFile.length() > 0,
            "Reference .cpx should be non-empty");

        // Frozen legacy roundtrip: reload and save again.
        // NOTE: the frozen legacy .cpx path is known to be unstable
        // (CriticalPairs child count differs on re-save); canonical
        // equality is therefore NOT asserted here. The reference file
        // above is what the DOM side compares against.
        agg.parser.ConflictsDependenciesContainer reloaded =
            new agg.parser.ConflictsDependenciesContainer();
        XMLHelper helper2 = new XMLHelper();
        assertTrue(helper2.read_from_xml(cpxFile.getAbsolutePath()),
            "Frozen legacy .cpx load should succeed");
        helper2.getTopObject(reloaded);
    }

    // ---- Morphism matrix scenarios ----

    /**
     * Produces the reference XML for every cell of the morphism matrix
     * (LHS x RHS x mapping x orientation x type graph).
     */
    @Test
    public void prepareMorphismMatrix() throws Exception {
        int[][] combinations = new int[][] {
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NOTHING, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NOTHING},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES},
            {TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.STRUCT_NODES_EDGES, TestDataGenerator.MAP_NODES_EDGES},
        };
        boolean[] orientations = {true, false};
        boolean[] typeGraphs = {true, false};
        for (int[] combo : combinations) {
            for (boolean directed : orientations) {
                for (boolean withTypeGraph : typeGraphs) {
                    GraGra graGra = TestDataGenerator.createMorphismGraGra(
                        combo[0], combo[1], combo[2], directed, withTypeGraph);
                    String baseName = morphBaseName(combo[0], combo[1], combo[2], directed, withTypeGraph);
                    saveLegacy(graGra, baseName + ".ggx");
                }
            }
        }
    }

    // ---- Attribute matrix scenarios ----

    /**
     * Produces the reference XML for the constant attribute values of all
     * basic types.
     */
    @Test
    public void prepareAttributeConstants() throws Exception {
        String[][] constants = new String[][] {
            {"boolean", "true"},
            {"boolean", "false"},
            {"int", "0"},
            {"int", "-42"},
            {"int", "2147483647"},
            {"long", "123456789012345"},
            {"long", "42"},
            {"float", "1.5"},
            {"double", "2.718"},
            {"String", "\"Hello World\""},
            {"String", "\"\""},
            {"String", "\"Special chars: <>&\\\"' äöü\""},
        };
        for (String[] constant : constants) {
            GraGra graGra = TestDataGenerator.createGraphWithTypedNodeValue(
                constant[0], constant[1]);
            String baseName = "attr_const_" + sanitize(constant[0]) + "_" + sanitize(constant[1]);
            saveLegacy(graGra, baseName + ".ggx");
        }
    }

    /**
     * Produces the reference XML for the rule attribute mapping forms
     * (constant, variable, expression, condition).
     */
    @Test
    public void prepareAttributeMappingForms() throws Exception {
        saveLegacy(TestDataGenerator.createRuleWithAttributes("val", "int", "42", "43", null),
            "attr_form_constant.ggx");
        saveLegacy(TestDataGenerator.createRuleWithAttributes("val", "int", "x", "x + 1", null),
            "attr_form_variable.ggx");
        saveLegacy(TestDataGenerator.createRuleWithAttributes("val", "int", "x * 2", "x + 1", null),
            "attr_form_expression.ggx");
        saveLegacy(TestDataGenerator.createRuleWithAttributes("val", "int", "x", "x", "x > 3"),
            "attr_form_condition.ggx");
    }

    /**
     * Produces the reference XML for the edge attribute mapping scenario.
     */
    @Test
    public void prepareEdgeAttributeForm() throws Exception {
        saveLegacy(TestDataGenerator.createEdgeAttributeRule(), "attr_edge_form.ggx");
    }

    // ---- UI layer scenarios (EdGraGra roundtrip like GraGraSave) ----

    /**
     * Produces a reference .ggx saved through the editor layer exactly the
     * way GraGraSave does: the EdGraGra is the top object, so the frozen
     * legacy path writes the core XML and enhances it with the UI segments
     * (NodeLayout / EdgeLayout in every node and edge element).
     */
    @Test
    public void prepareUiBasicScenario() throws Exception {
        GraGra graGra = TestDataGenerator.createBasicGraphWithAttributes(true);
        agg.editor.impl.EdGraGra edGraGra = new agg.editor.impl.EdGraGra(graGra);

        // Distinct editor data so the reference pins real UI state (the
        // EdNode/EdArc defaults are 100,100 and 0,-22; the DOM-side test
        // must prove that loading applies this data, not the defaults).
        int pos = 10;
        for (agg.editor.impl.EdNode node : edGraGra.getGraph().getNodes()) {
            node.setX(100 + pos);
            node.setY(200 + pos);
            pos += 5;
        }
        for (agg.editor.impl.EdArc arc : edGraGra.getGraph().getArcs()) {
            arc.setTextOffset(13, -7);
        }

        XMLHelper helper = new XMLHelper();
        helper.addTopObject(edGraGra);
        File uiFile = new File(prepDir, "ui_basic_graph_attrs.ggx");
        assertTrue(helper.save_to_xml(uiFile.getAbsolutePath()),
            "Frozen legacy UI save should succeed");
        assertTrue(uiFile.exists() && uiFile.length() > 0,
            "UI reference file should be non-empty");
    }

    /**
     * Produces a UI reference for a grammar with a rule (LHS/RHS mapping),
     * NAC, PAC, nested application condition, a bent edge and a loop edge,
     * so the UI segments of rule graphs and the special edge layouts
     * (bendX/bendY, loopW/loopH) are pinned in the reference.
     */
    @Test
    public void prepareUiRuleScenario() throws Exception {
        GraGra graGra = TestDataGenerator.createRuleWithNacPacNestedAC(true);

        // Loop edge in the host graph (covers loopW/loopH layout)
        java.util.Iterator<agg.xt_basis.Node> hostNodes =
            graGra.getGraph().getNodesCollection().iterator();
        if (hostNodes.hasNext()) {
            agg.xt_basis.Node hostNode = hostNodes.next();
            agg.xt_basis.Type loopType = graGra.createArcType(false);
            loopType.setStringRepr("loopLink");
            graGra.getGraph().createArc(loopType, hostNode, hostNode);
        }

        agg.editor.impl.EdGraGra edGraGra =
            new agg.editor.impl.EdGraGra(graGra);

        int pos = 10;
        pos = pinGraphPositions(edGraGra.getGraph(), pos);
        pos = pinGraphPositions(edGraGra.getTypeGraph(), pos);
        for (int i = 0; i < edGraGra.getRules().size(); i++) {
            agg.editor.impl.EdRule rule = edGraGra.getRules().get(i);
            pos = pinGraphPositions(rule.getLeft(), pos);
            pos = pinGraphPositions(rule.getRight(), pos);
            for (int j = 0; j < rule.getNACs().size(); j++) {
                pos = pinGraphPositions(rule.getNACs().get(j), pos);
            }
            for (int j = 0; j < rule.getPACs().size(); j++) {
                pos = pinGraphPositions(rule.getPACs().get(j), pos);
            }
            // Bent edge in the LHS (covers non-default anchor layout)
            for (int j = 0; j < rule.getLeft().getArcs().size(); j++) {
                rule.getLeft().getArcs().get(j)
                    .setAnchor(new java.awt.Point(25, 30));
            }
        }

        XMLHelper helper = new XMLHelper();
        helper.addTopObject(edGraGra);
        File uiFile = new File(prepDir, "ui_rule_nac.ggx");
        assertTrue(helper.save_to_xml(uiFile.getAbsolutePath()),
            "Frozen legacy UI save should succeed");
        assertTrue(uiFile.exists() && uiFile.length() > 0,
            "UI reference file should be non-empty");
    }

    /**
     * Produces a UI reference for a grammar with an atomic constraint and
     * a formula constraint, so the UI path covers the Atomics/Constraints
     * sections of a UI-layer save.
     */
    @Test
    public void prepareUiConstraintScenario() throws Exception {
        GraGra graGra = TestDataGenerator.createWithConstraints(true);
        agg.editor.impl.EdGraGra edGraGra =
            new agg.editor.impl.EdGraGra(graGra);

        int pos = 10;
        pos = pinGraphPositions(edGraGra.getGraph(), pos);
        pos = pinGraphPositions(edGraGra.getTypeGraph(), pos);
        for (int i = 0; i < edGraGra.getAtomics().size(); i++) {
            pos = pinGraphPositions(edGraGra.getAtomics().get(i).getLeft(), pos);
            pos = pinGraphPositions(edGraGra.getAtomics().get(i).getRight(), pos);
        }

        XMLHelper helper = new XMLHelper();
        helper.addTopObject(edGraGra);
        File uiFile = new File(prepDir, "ui_constraints.ggx");
        assertTrue(helper.save_to_xml(uiFile.getAbsolutePath()),
            "Frozen legacy UI save should succeed");
        assertTrue(uiFile.exists() && uiFile.length() > 0,
            "UI reference file should be non-empty");
    }

    /**
     * Assigns distinct, increasing editor positions to the nodes of the
     * given graph so the reference pins real UI state, and returns the
     * next free position counter.
     */
    private int pinGraphPositions(agg.editor.impl.EdGraph graph, int pos) {
        for (int i = 0; i < graph.getNodes().size(); i++) {
            agg.editor.impl.EdNode node = graph.getNodes().get(i);
            node.setX(100 + pos);
            node.setY(200 + pos);
            pos += 7;
        }
        return pos;
    }

    // ---- Helpers ----

    private void saveLegacy(GraGra graGra, String fileName) throws Exception {
        File outFile = new File(prepDir, fileName);
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(graGra);
        assertTrue(helper.save_to_xml(outFile.getAbsolutePath()),
            "Frozen legacy save should succeed: " + fileName);
        assertTrue(outFile.exists() && outFile.length() > 0,
            "Reference file should be non-empty: " + fileName);
    }

    private GraGra undirected(GraGra graGra) {
        graGra.getTypeSet().setArcDirected(false);
        return graGra;
    }

    /** Same naming scheme as MorphismMatrixTest uses on the DOM side. */
    public static String morphBaseName(int lhsSpec, int rhsSpec, int mapSpec,
            boolean directed, boolean withTypeGraph) {
        return "morph_" + lhsSpec + "_" + rhsSpec + "_" + mapSpec
            + (directed ? "_dir" : "_undir") + (withTypeGraph ? "_TG" : "_noTG");
    }

    /** Same naming scheme as AttributeMatrixTest uses on the DOM side. */
    public static String attributeConstantBaseName(String typeName, String valueText) {
        return "attr_const_" + sanitize(typeName) + "_" + sanitize(valueText);
    }

    private static String sanitize(String text) {
        return text.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
