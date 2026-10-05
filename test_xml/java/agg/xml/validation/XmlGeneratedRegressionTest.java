/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xt_basis.GraGra;
import agg.xt_basis.Node;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xml.XMLSerialization;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * DOM-side regression tests using programmatically generated structures.
 *
 * <p>The generation and the legacy save ran in the PREPARATION phase
 * against the FROZEN legacy clone (agg-core-legacy): the suite
 * {@code agg.xml.prep.LegacyPreparationTest} produced reference files
 * {@code target/legacy_prep/gen_<base>.ggx}. This test is the "actual"
 * half: it runs against the CURRENT code in src and verifies</p>
 * <ol>
 *   <li>the new DOM path can load the frozen reference XML,</li>
 *   <li>the DOM save produces canonically identical XML,</li>
 *   <li>structure survives the DOM roundtrip.</li>
 * </ol>
 *
 * <p>Structure groups: basic graph + attributes, rule with NAC/PAC/nested
 * AC, constraints, match, rule scheme, rule sequence, composite of all
 * element types. Each group runs directed and undirected, with and
 * without type graph.</p>
 */
public class XmlGeneratedRegressionTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/xml-gen-regression/";

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

    // ---- Group 1: Basic graph + types + attributes ----

    @Test
    public void testBasicGraphWithAttributes() throws Exception {
        runDomRoundtrip("basic_graph_attrs");
    }

    // ---- Group 2: Rule with NAC, PAC, nested AC ----

    @Test
    public void testRuleWithNacPacNestedAC() throws Exception {
        runDomRoundtrip("rule_nac_pac");
    }

    // ---- Group 3: Constraints ----

    @Test
    public void testWithConstraints() throws Exception {
        runDomRoundtrip("constraints");
    }

    // ---- Group 4: Match ----

    @Test
    public void testWithMatch() throws Exception {
        runDomRoundtrip("match");
    }

    // ---- Group 5: RuleScheme (KernelRule / MultiRule) ----

    @Test
    public void testWithRuleScheme() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme");
    }

    // ---- Group 6: RuleSequence ----

    @Test
    public void testWithRuleSequence() throws Exception {
        runDomRoundtrip("rule_sequence");
    }

    // ---- Composite of all element types ----

    @Test
    public void testCompositeAllElementTypes() throws Exception {
        runDomRoundtrip("composite_all");
    }

    // ---- Undirected variants ----

    @Test
    public void testBasicGraphWithAttributesUndirected() throws Exception {
        runDomRoundtrip("basic_graph_attrs_undirected");
    }

    @Test
    public void testRuleWithNacPacNestedACUndirected() throws Exception {
        runDomRoundtrip("rule_nac_pac_undirected");
    }

    @Test
    public void testWithConstraintsUndirected() throws Exception {
        runDomRoundtrip("constraints_undirected");
    }

    @Test
    public void testWithMatchUndirected() throws Exception {
        runDomRoundtrip("match_undirected");
    }

    @Test
    public void testWithRuleSequenceUndirected() throws Exception {
        runDomRoundtrip("rule_sequence_undirected");
    }

    @Test
    public void testWithRuleSchemeUndirected() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_undirected");
    }

    @Test
    public void testWithRuleSchemeNac() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_nac");
    }

    @Test
    public void testWithRuleSchemeNacUndirected() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_nac_undirected");
    }

    @Test
    public void testCompositeAllElementTypesUndirected() throws Exception {
        runDomRoundtrip("composite_all_undirected");
    }

    // ---- No type graph variants ----

    @Test
    public void testBasicGraphWithAttributesNoTG() throws Exception {
        runDomRoundtrip("basic_graph_attrs_noTG");
    }

    @Test
    public void testBasicGraphWithAttributesUndirectedNoTG() throws Exception {
        runDomRoundtrip("basic_graph_attrs_undirected_noTG");
    }

    @Test
    public void testRuleWithNacPacNestedACNoTG() throws Exception {
        runDomRoundtrip("rule_nac_pac_noTG");
    }

    @Test
    public void testRuleWithNacPacNestedACUndirectedNoTG() throws Exception {
        runDomRoundtrip("rule_nac_pac_undirected_noTG");
    }

    @Test
    public void testWithConstraintsNoTG() throws Exception {
        runDomRoundtrip("constraints_noTG");
    }

    @Test
    public void testWithConstraintsUndirectedNoTG() throws Exception {
        runDomRoundtrip("constraints_undirected_noTG");
    }

    @Test
    public void testWithMatchNoTG() throws Exception {
        runDomRoundtrip("match_noTG");
    }

    @Test
    public void testWithMatchUndirectedNoTG() throws Exception {
        runDomRoundtrip("match_undirected_noTG");
    }

    @Test
    public void testWithRuleSchemeNoTG() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_noTG");
    }

    @Test
    public void testWithRuleSchemeUndirectedNoTG() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_undirected_noTG");
    }

    @Test
    public void testWithRuleSchemeNacNoTG() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_nac_noTG");
    }

    @Test
    public void testWithRuleSchemeNacUndirectedNoTG() throws Exception {
        runRuleSchemeRoundtrip("rule_scheme_nac_undirected_noTG");
    }

    @Test
    public void testWithRuleSequenceNoTG() throws Exception {
        runDomRoundtrip("rule_sequence_noTG");
    }

    @Test
    public void testCompositeAllElementTypesNoTG() throws Exception {
        runDomRoundtrip("composite_all_noTG");
    }

    @Test
    public void testWithRuleSequenceUndirectedNoTG() throws Exception {
        runDomRoundtrip("rule_sequence_undirected_noTG");
    }

    @Test
    public void testCompositeAllElementTypesUndirectedNoTG() throws Exception {
        runDomRoundtrip("composite_all_undirected_noTG");
    }

    // ---- Core DOM roundtrip methods ----

    /**
     * Loads the frozen reference XML with the DOM path, saves with the DOM
     * path and compares canonically against the reference.
     */
    private void runDomRoundtrip(String baseName) throws Exception {
        File refFile = prepFile(baseName);

        // Step 1: load frozen reference with new DOM path
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, refFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load of the frozen reference should produce a named GraGra: " + baseName);

        // Step 2: save with new DOM path
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + baseName);
        assertTrue(newFile.exists() && newFile.length() > 0,
            "New XML file should be non-empty: " + baseName);

        // Step 3: canonical comparison against the frozen reference
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + baseName + ": " + result.getMessage());
    }

    /**
     * RuleScheme variant: verifies the kernel rule and multi rules survive
     * the DOM roundtrip of the frozen reference, then reloads the DOM save
     * and compares rule counts.
     */
    private void runRuleSchemeRoundtrip(String baseName) throws Exception {
        File refFile = prepFile(baseName);

        // Step 1: load frozen reference with new DOM path
        GraGra graGraNew = new GraGra();
        XMLSerialization.loadWithDom(graGraNew, refFile.getAbsolutePath());
        assertNotNull(graGraNew.getName(),
            "New DOM load should produce a named GraGra: " + baseName);

        // Step 2: verify the rule scheme structure
        boolean foundRuleScheme = false;
        for (Rule r : graGraNew.getRulesVec()) {
            if (r.getRuleScheme() != null) {
                foundRuleScheme = true;
                assertNotNull(r.getRuleScheme().getKernelRule(),
                    "Kernel rule should exist: " + baseName);
                assertTrue(r.getRuleScheme().getMultiRules().size() > 0,
                    "Multi rules should exist: " + baseName);
                assertTrue(r.getRuleScheme().getKernelRule().getLeft().getNodesCount() > 0,
                    "Kernel rule LHS should have nodes: " + baseName);
                assertTrue(r.getRuleScheme().getKernelRule().getRight().getNodesCount() > 0,
                    "Kernel rule RHS should have nodes: " + baseName);
            }
        }
        assertTrue(foundRuleScheme, "RuleScheme should be loaded: " + baseName);

        // Step 3: save with the new DOM path and verify it can be reloaded
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraNew, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + baseName);
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile.getAbsolutePath());
        assertEquals(graGraReloaded.getRulesVec().size(), graGraNew.getRulesVec().size(),
            "Rule count should match after reload: " + baseName);

        // Step 4: canonical comparison against the frozen reference
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + baseName + ": " + result.getMessage());
    }

    private File prepFile(String baseName) {
        File refFile = new File(PREP_DIR, "gen_" + baseName + ".ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference file is missing (run the legacy preparation "
                + "suite first: mvn test at the parent, or -pl test/test_agg/legacy_agg): "
                + refFile.getPath());
        return refFile;
    }


    // ---- Group 8: .cpx computed critical pairs (in-memory save) ----

    /**
     * Saves a freshly generated computed-pairs container with the DOM path
     * and compares canonically against the RAW legacy reference of the
     * same generated structure, then reloads and re-saves for stability.
     * Only the plain and NAC variants are compared against the RAW
     * reference: the PAC variant is lossy in the reader, so it has no
     * stable canonical form.
     */
    private void runCpaComputedRoundtrip(String baseName, GraGra gra,
            String refName) throws Exception {
        agg.parser.ConflictsDependenciesContainer cdc =
            TestDataGenerator.createComputedConflictsDependenciesContainer(gra);

        // Step 1: save the in-memory container with the DOM path
        File outFile = new File(outputDir, baseName + "_new.cpx");
        assertTrue(XMLSerialization.saveWithDom(cdc, outFile.getAbsolutePath()),
            "DOM .cpx save should succeed: " + baseName);
        assertTrue(outFile.exists() && outFile.length() > 0,
            "DOM .cpx output should be non-empty: " + baseName);

        // Step 2: canonical comparison against the RAW legacy reference
        File refFile = new File(PREP_DIR, refName);
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference .cpx is missing (run the legacy preparation"
                + " suite first): " + refFile.getPath());
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + refName + ": "
                + result.getMessage());

        // Step 3: DOM reload and re-save stability
        agg.parser.ConflictsDependenciesContainer reloaded =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        File stableFile = new File(outputDir, baseName + "_stable.cpx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM .cpx stability save should succeed: " + baseName);
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for " + baseName + ": "
                + stableResult.getMessage());
    }

    @Test
    public void testCpaComputedPlain() throws Exception {
        runCpaComputedRoundtrip("cpa_computed_plain",
            TestDataGenerator.createCpaGraGra(),
            "gen_conflicts_computed.cpx");
    }

    @Test
    public void testCpaComputedNac() throws Exception {
        runCpaComputedRoundtrip("cpa_computed_nac",
            TestDataGenerator.createCpaNacGraGra(),
            "gen_conflicts_computed_nac.cpx");
    }


    // ---- Group 9: stress scenarios ----

    /**
     * Stress scenario with 101 small rules that vary layer, priority,
     * LHS/RHS structure, attribute forms (constant, unset, variable,
     * expression, condition), attribute types, object names, graph
     * comments and NAC/PAC/nested AC conditions. Verifies the fresh
     * in-memory DOM save against the frozen legacy reference, the DOM
     * load/save roundtrip canonically against the same reference,
     * the DOM stability over a second roundtrip, and the structural
     * survival of the rule variations.
     */
    @Test
    public void testStressRules101() throws Exception {
        File refFile = prepFile("stress_rules_101");

        // Step 1: fresh in-memory grammar, DOM save, canonical vs RAW
        GraGra fresh = TestDataGenerator.createStressRulesGraGra();
        assertEquals(fresh.getListOfRules().size(), 101,
            "The generator should create 101 rules");
        File memFile = new File(outputDir, "stress_rules_101_mem.ggx");
        assertTrue(XMLSerialization.saveWithDom(fresh, memFile.getAbsolutePath()),
            "In-memory DOM save should succeed for the stress grammar");
        XmlCanonicalComparator.ComparisonResult memResult =
            XmlCanonicalComparator.compareFiles(refFile, memFile);
        assertTrue(memResult.isEqual(),
            "In-memory DOM save mismatch for gen_stress_rules_101.ggx: "
                + memResult.getMessage());

        // Step 2: DOM load of the RAW reference with structural checks
        GraGra loaded = new GraGra();
        XMLSerialization.loadWithDom(loaded, refFile.getAbsolutePath());
        assertStressRules(loaded);

        // Step 3: DOM save, canonical comparison against the RAW reference
        File outFile = new File(outputDir, "stress_rules_101_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(loaded, outFile.getAbsolutePath()),
            "DOM save should succeed for the stress grammar");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for gen_stress_rules_101.ggx: "
                + result.getMessage());

        // Step 4: DOM stability: reload and save again
        GraGra reloaded = new GraGra();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        assertStressRules(reloaded);
        File stableFile = new File(outputDir, "stress_rules_101_stable.ggx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM stability save should succeed for the stress grammar");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for the stress grammar: "
                + stableResult.getMessage());
    }

    /**
     * Asserts the structural survival of the stress grammar: 101
     * rules, the layer/priority scheme, and the counts of rules with
     * NAC (i % 7 == 3), PAC (i % 11 == 5) and nested AC
     * (i % 13 == 7) for i in [0, 100].
     */
    private void assertStressRules(GraGra gra) {
        assertEquals(gra.getListOfRules().size(), 101,
            "All 101 stress rules should survive the DOM roundtrip");

        int nacCount = 0;
        int pacCount = 0;
        int acCount = 0;
        for (Rule r : gra.getListOfRules()) {
            if (!r.getNACsList().isEmpty()) {
                nacCount++;
            }
            if (!r.getPACsList().isEmpty()) {
                pacCount++;
            }
            if (!r.getNestedACsList().isEmpty()) {
                acCount++;
            }
        }
        assertEquals(nacCount, 14, "rules with a NAC (i % 7 == 3)");
        assertEquals(pacCount, 9, "rules with a PAC (i % 11 == 5)");
        assertEquals(acCount, 8, "rules with a nested AC (i % 13 == 7)");

        // spot checks derived from the generator scheme
        Rule r007 = findStressRule(gra, "stressRule007");
        assertEquals(r007.getLayer(), 3, "layer of stressRule007 (7 % 4)");
        assertEquals(r007.getPriority(), 7, "priority of stressRule007 (7 % 10)");
        assertEquals(r007.getLeft().getNodesCount(), 2, "LHS node count of stressRule007 (1 % 3 + 1)");

        Rule r100 = findStressRule(gra, "stressRule100");
        assertEquals(r100.getLayer(), 0, "layer of stressRule100 (100 % 4)");
        assertEquals(r100.getPriority(), 0, "priority of stressRule100 (100 % 10)");
        assertEquals(r100.getLeft().getNodesCount(), 2, "LHS node count of stressRule100 (1 % 3 + 1)");
        assertTrue(r100.getLeft().getArcsCount() > 0,
            "stressRule100 should keep its LHS edge (even index)");
        assertEquals(r100.getTarget().getNodesCount(), 2,
            "stressRule100 is the identity variant (100 % 5 == 0)");
    }

    private Rule findStressRule(GraGra gra, String name) {
        for (Rule r : gra.getListOfRules()) {
            if (name.equals(r.getName())) {
                return r;
            }
        }
        fail("Stress rule not found: " + name);
        return null;
    }

    /**
     * Stress scenario with a single rule that carries 101 NACs and
     * 101 PACs with small parameter variations of the contained
     * objects. Verifies the fresh in-memory DOM save against the
     * frozen legacy reference, the DOM load/save roundtrip
     * canonically against the same reference, the DOM stability over
     * a second roundtrip, and the structural survival of the
     * conditions.
     */
    @Test
    public void testStressConditions101() throws Exception {
        File refFile = prepFile("stress_conditions_101");

        // Step 1: fresh in-memory grammar, DOM save, canonical vs RAW
        GraGra fresh = TestDataGenerator.createStressConditionsGraGra();
        assertEquals(fresh.getListOfRules().size(), 1,
            "The generator should create a single stress rule");
        assertEquals(fresh.getListOfRules().get(0).getNACsList().size(), 101,
            "The generator should create 101 NACs");
        assertEquals(fresh.getListOfRules().get(0).getPACsList().size(), 101,
            "The generator should create 101 PACs");
        File memFile = new File(outputDir, "stress_conditions_101_mem.ggx");
        assertTrue(XMLSerialization.saveWithDom(fresh, memFile.getAbsolutePath()),
            "In-memory DOM save should succeed for the conditions stress grammar");
        XmlCanonicalComparator.ComparisonResult memResult =
            XmlCanonicalComparator.compareFiles(refFile, memFile);
        assertTrue(memResult.isEqual(),
            "In-memory DOM save mismatch for gen_stress_conditions_101.ggx: "
                + memResult.getMessage());

        // Step 2: DOM load of the RAW reference with structural checks
        GraGra loaded = new GraGra();
        XMLSerialization.loadWithDom(loaded, refFile.getAbsolutePath());
        assertStressConditions(loaded);

        // Step 3: DOM save, canonical comparison against the RAW reference
        File outFile = new File(outputDir, "stress_conditions_101_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(loaded, outFile.getAbsolutePath()),
            "DOM save should succeed for the conditions stress grammar");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for gen_stress_conditions_101.ggx: "
                + result.getMessage());

        // Step 4: DOM stability: reload and save again
        GraGra reloaded = new GraGra();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        assertStressConditions(reloaded);
        File stableFile = new File(outputDir, "stress_conditions_101_stable.ggx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM stability save should succeed for the conditions stress grammar");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for the conditions stress grammar: "
                + stableResult.getMessage());
    }

    /**
     * Asserts the structural survival of the conditions stress grammar:
     * one rule with 101 NACs and 101 PACs, plus spot checks derived
     * from the generator scheme (mapped LHS nodes, own node count,
     * edges, attribute values).
     */
    private void assertStressConditions(GraGra gra) {
        assertEquals(gra.getListOfRules().size(), 1,
            "The single stress rule should survive the DOM roundtrip");
        Rule rule = gra.getListOfRules().get(0);
        assertEquals(rule.getName(), "stressConditionsRule",
            "The stress rule should keep its name");
        assertEquals(rule.getNACsList().size(), 101,
            "All 101 NACs should survive the DOM roundtrip");
        assertEquals(rule.getPACsList().size(), 101,
            "All 101 PACs should survive the DOM roundtrip");
        assertTrue(rule.getNestedACsList().isEmpty(),
            "The stress rule should not carry nested ACs");

        // spot checks derived from the generator scheme:
        // i = 7: two own nodes (7 % 2 == 1), only lhs1 mapped
        //        (7 % 4 == 3), no edge (7 % 3 != 0)
        OrdinaryMorphism nac7 = findStressCondition(
            rule.getNACsList(), "stressNac007");
        assertEquals(nac7.getTarget().getNodesCount(), 3,
            "stressNac007 should carry 2 own nodes plus the lhs1 image");
        assertEquals(nac7.getTarget().getArcsCount(), 0,
            "stressNac007 should not carry an edge (7 % 3 != 0)");

        // i = 100: one own node (100 % 2 == 0), only lhs1 mapped
        //          (100 % 4 == 0), values set (100 % 5 != 3)
        OrdinaryMorphism pac100 = findStressCondition(
            rule.getPACsList(), "stressPac100");
        assertEquals(pac100.getTarget().getNodesCount(), 2,
            "stressPac100 should carry 1 own node plus the lhs1 image");
        Node own100 = null;
        for (Node n : pac100.getTarget().getNodesSet()) {
            if (!pac100.hasInverseImage(n)) {
                own100 = n;
            }
        }
        assertNotNull(own100,
            "stressPac100 should carry one own node");
        agg.attribute.impl.ValueMember val100 =
            (agg.attribute.impl.ValueMember) own100.getAttribute().getMemberAt("val");
        assertEquals(val100.getExprAsText(), "100"
            , "the own node of stressPac100 should keep its int value");
        agg.attribute.impl.ValueMember tag100 =
            (agg.attribute.impl.ValueMember) own100.getAttribute().getMemberAt("tag");
        assertEquals(tag100.getExprAsText(), "\"c100\""
            , "the own node of stressPac100 should keep its String value");
    }

    private OrdinaryMorphism findStressCondition(
            java.util.List<OrdinaryMorphism> conditions, String name) {
        for (OrdinaryMorphism cond : conditions) {
            if (name.equals(cond.getName())) {
                return cond;
            }
        }
        fail("Stress condition not found: " + name);
        return null;
    }

    /**
     * Resolves a generated .cpx reference produced by the legacy
     * preparation suite.
     */
    private File prepCpxFile(String filename) {
        File refFile = new File(PREP_DIR, filename);
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Generated .cpx reference is missing (run the legacy preparation"
                + " suite first): " + refFile.getPath());
        return refFile;
    }

    /**
     * Structural stress scenario for the .cpx rule sets: both
     * containers list all 101 rules (indexed references i0 .. i100)
     * without computed entries. Verifies the DOM roundtrip
     * canonically against the raw legacy reference and the DOM
     * stability, exercising the canonical normalization of the
     * multi-digit indexed references.
     */
    @Test
    public void testStressRulesCpx101() throws Exception {
        File refFile = prepCpxFile("gen_stress_rules_101.cpx");

        // Step 1: fresh in-memory container, DOM save, canonical vs RAW
        GraGra freshGra = TestDataGenerator.createStressRulesGraGra();
        agg.parser.ConflictsDependenciesContainer fresh =
            TestDataGenerator.createStressRulesCpxContainer(freshGra);
        File memFile = new File(outputDir, "stress_rules_101_cpx_mem.cpx");
        assertTrue(XMLSerialization.saveWithDom(fresh, memFile.getAbsolutePath()),
            "In-memory DOM save should succeed for the stress .cpx");
        XmlCanonicalComparator.ComparisonResult memResult =
            XmlCanonicalComparator.compareFiles(refFile, memFile);
        assertTrue(memResult.isEqual(),
            "In-memory DOM save mismatch for gen_stress_rules_101.cpx: "
                + memResult.getMessage());

        // Step 2: DOM load of the RAW reference
        agg.parser.ConflictsDependenciesContainer cdc =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(cdc, refFile.getAbsolutePath());
        assertNotNull(cdc.getExcludePairContainer(),
            "DOM load should create the conflict container");
        assertEquals(cdc.getExcludePairContainer().getRules().size(), 101,
            "The conflict container should list all 101 rules");
        assertEquals(cdc.getDependencyPairContainer().getRules().size(), 101,
            "The dependency container should list all 101 rules");

        // Step 3: DOM save, canonical comparison against the RAW reference
        File outFile = new File(outputDir, "stress_rules_101_cpx_new.cpx");
        assertTrue(XMLSerialization.saveWithDom(cdc, outFile.getAbsolutePath()),
            "DOM save should succeed for the stress .cpx");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for gen_stress_rules_101.cpx: "
                + result.getMessage());

        // Step 4: DOM stability: reload and save again
        agg.parser.ConflictsDependenciesContainer reloaded =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        File stableFile = new File(outputDir, "stress_rules_101_cpx_stable.cpx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM stability save should succeed for the stress .cpx");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for the stress .cpx: "
                + stableResult.getMessage());
    }

    /**
     * Computed plain pairs with an attached CPA basis graph: the
     * ConflictDependencyGraph section (types + graph) is written by
     * the DOM path and reconstructed on load; verified canonically
     * against the raw legacy reference and for DOM stability.
     */
    @Test
    public void testCpaComputedCpaGraph() throws Exception {
        File refFile = prepCpxFile("gen_conflicts_computed_cpagraph.cpx");

        // Step 1: fresh in-memory container, DOM save, canonical vs RAW
        GraGra freshGra = TestDataGenerator.createCpaGraGra();
        agg.parser.ConflictsDependenciesContainer fresh =
            TestDataGenerator.createCpaGraphConflictsDependenciesContainer(freshGra);
        File memFile = new File(outputDir, "conflicts_computed_cpagraph_mem.cpx");
        assertTrue(XMLSerialization.saveWithDom(fresh, memFile.getAbsolutePath()),
            "In-memory DOM save should succeed for the CPA graph .cpx");
        XmlCanonicalComparator.ComparisonResult memResult =
            XmlCanonicalComparator.compareFiles(refFile, memFile);
        assertTrue(memResult.isEqual(),
            "In-memory DOM save mismatch for gen_conflicts_computed_cpagraph.cpx: "
                + memResult.getMessage());

        // Step 2: DOM load of the RAW reference with structural checks
        agg.parser.ConflictsDependenciesContainer cdc =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(cdc, refFile.getAbsolutePath());
        assertNotNull(cdc.getCPABasisGraph(),
            "DOM load should reconstruct the CPA basis graph");
        assertEquals(cdc.getCPABasisGraph().getNodesCount(), 2,
            "The CPA basis graph should carry its two nodes");
        assertEquals(cdc.getCPABasisGraph().getArcsCount(), 1,
            "The CPA basis graph should carry its arc");

        // Step 3: DOM save, canonical comparison against the RAW reference
        File outFile = new File(outputDir, "conflicts_computed_cpagraph_new.cpx");
        assertTrue(XMLSerialization.saveWithDom(cdc, outFile.getAbsolutePath()),
            "DOM save should succeed for the CPA graph .cpx");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for gen_conflicts_computed_cpagraph.cpx: "
                + result.getMessage());

        // Step 4: DOM stability: reload and save again
        agg.parser.ConflictsDependenciesContainer reloaded =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        assertNotNull(reloaded.getCPABasisGraph(),
            "The CPA basis graph should survive the stability reload");
        File stableFile = new File(outputDir, "conflicts_computed_cpagraph_stable.cpx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM stability save should succeed for the CPA graph .cpx");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for the CPA graph .cpx: "
                + stableResult.getMessage());
    }

    /**
     * Free container entry states over the layered/disabled
     * grammar: the DISABLED state of the entry over the disabled
     * rule and the NOT_RELATED state of the entry over rules on
     * different layers are restored by the DOM load and match the
     * legacy load of the same file; the DOM save is compared
     * canonically against the raw legacy reference.
     */
    @Test
    public void testCpaComputedFreeStates() throws Exception {
        File refFile = prepCpxFile("gen_conflicts_computed_freestates.cpx");

        // Step 1: DOM load of the RAW reference
        agg.parser.ConflictsDependenciesContainer cdc =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(cdc, refFile.getAbsolutePath());
        assertNotNull(cdc.getExcludePairContainer(),
            "DOM load should create the conflict container");
        Rule deleteItem = findStressRule(cdc.getGrammar(), "deleteItem");
        Rule useItem = findStressRule(cdc.getGrammar(), "useItem");
        Rule createItem = findStressRule(cdc.getGrammar(), "createItem");
        agg.parser.ExcludePairContainer.Entry disabledEntry =
            cdc.getExcludePairContainer().getEntry(deleteItem, useItem);
        assertNotNull(disabledEntry,
            "the free entry over the disabled rule should be restored");
        assertEquals(disabledEntry.getState(),
            agg.parser.ExcludePairContainer.Entry.DISABLED,
            "the entry over the disabled rule should be DISABLED after the DOM load");
        agg.parser.ExcludePairContainer.Entry unrelatedEntry =
            cdc.getExcludePairContainer().getEntry(useItem, createItem);
        assertNotNull(unrelatedEntry,
            "the free entry over the layer mismatch should be restored");
        assertEquals(unrelatedEntry.getState(),
            agg.parser.ExcludePairContainer.Entry.NOT_RELATED,
            "the entry over rules on different layers should be"
                + " NOT_RELATED after the DOM load");

        // Step 2: legacy load of the same file as the cross-system oracle
        agg.util.XMLHelper helper = new agg.util.XMLHelper();
        assertTrue(helper.read_from_xml(refFile.getAbsolutePath()),
            "Legacy load of the free states reference should succeed");
        agg.parser.ConflictsDependenciesContainer legacy =
            new agg.parser.ConflictsDependenciesContainer();
        helper.getTopObject(legacy);
        agg.parser.ExcludePairContainer legacyEpc =
            legacy.getLayeredExcludePairContainer() != null
                ? legacy.getLayeredExcludePairContainer()
                : legacy.getExcludePairContainer();
        assertNotNull(legacyEpc,
            "Legacy load should create a conflict container");
        Rule legacyDelete = findStressRule(legacy.getGrammar(), "deleteItem");
        Rule legacyUse = findStressRule(legacy.getGrammar(), "useItem");
        Rule legacyCreate = findStressRule(legacy.getGrammar(), "createItem");
        assertEquals(legacyEpc.getEntry(legacyDelete, legacyUse).getState(),
            agg.parser.ExcludePairContainer.Entry.DISABLED,
            "the legacy reader should restore the same DISABLED state");
        assertEquals(legacyEpc.getEntry(legacyUse, legacyCreate).getState(),
            agg.parser.ExcludePairContainer.Entry.NOT_RELATED,
            "the legacy reader should restore the same NOT_RELATED state");

        // Step 3: DOM save, canonical comparison against the RAW reference
        File outFile = new File(outputDir, "conflicts_computed_freestates_new.cpx");
        assertTrue(XMLSerialization.saveWithDom(cdc, outFile.getAbsolutePath()),
            "DOM save should succeed for the free states .cpx");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for gen_conflicts_computed_freestates.cpx: "
                + result.getMessage());

        // Step 4: DOM stability: reload and save again
        agg.parser.ConflictsDependenciesContainer reloaded =
            new agg.parser.ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(reloaded, outFile.getAbsolutePath());
        File stableFile = new File(outputDir, "conflicts_computed_freestates_stable.cpx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, stableFile.getAbsolutePath()),
            "DOM stability save should succeed for the free states .cpx");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "DOM path should be stable for the free states .cpx: "
                + stableResult.getMessage());
    }}
