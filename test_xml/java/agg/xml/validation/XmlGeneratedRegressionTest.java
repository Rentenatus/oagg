/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xt_basis.GraGra;
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
}
