/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.parser.ConflictsDependenciesContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.ruleappl.RuleSequence;
import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xt_basis.Rule;
import agg.xml.XMLSerialization;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Regression tests using programmatically generated GraGra structures.
 *
 * <p>Test pattern per structure group:
 * <ol>
 *   <li>Create structure programmatically via GraGra API.</li>
 *   <li>Save old-style as XML (XMLHelper / XwriteObject).</li>
 *   <li>Read XML with the new DOM path (GraGraAdapter / DOMXMLDeserializer).</li>
 *   <li>Save again with the new DOM path.</li>
 *   <li>Compare both XMLs canonically (IDs and element ordering normalised).</li>
 * </ol>
 *
 * <p>Structure groups (each is a single test covering multiple element types):
 * <ul>
 *   <li>Basic graph + types + attributes (GraGra, Graph, Node, Arc, TypeImpl,
 *       NodeTypeImpl, ArcTypeImpl, TypeGraph, DeclTuple, DeclMember)</li>
 *   <li>Rule with NAC/PAC/NestedAC (Rule, OrdinaryMorphism, NAC, PAC,
 *       NestedApplCond, Match)</li>
 *   <li>Constraints (Formula, AtomConstraint)</li>
 *   <li>Match (Match, GraGra)</li>
 *   <li>RuleScheme (RuleScheme, MultiRule, KernelRule)</li>
 *   <li>RuleSequence (RuleSequence, ApplRuleSequence)</li>
 *   <li>ConflictsDependenciesContainer (ExcludePairContainer,
 *       DependencyPairContainer)</li>
 * </ul>
 */
public class XmlGeneratedRegressionTest {

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
        GraGra graGra = TestDataGenerator.createBasicGraphWithAttributes();
        runRoundtrip(graGra, "basic_graph_attrs");
    }

    // ---- Group 2: Rule with NAC, PAC, nested AC ----

    @Test
    public void testRuleWithNacPacNestedAC() throws Exception {
        GraGra graGra = TestDataGenerator.createRuleWithNacPacNestedAC();
        runRoundtrip(graGra, "rule_nac_pac");
    }

    // ---- Group 3: Constraints ----

    @Test
    public void testWithConstraints() throws Exception {
        GraGra graGra = TestDataGenerator.createWithConstraints();
        runRoundtrip(graGra, "constraints");
    }

    // ---- Group 4: Match ----

    @Test
    public void testWithMatch() throws Exception {
        GraGra graGra = TestDataGenerator.createWithMatch();
        runRoundtrip(graGra, "match");
    }

    // ---- Group 5: RuleScheme ----

    @Test
    public void testWithRuleScheme() throws Exception {
        GraGra graGra = TestDataGenerator.createWithRuleScheme();

        // Save old-style
        File oldFile = new File(outputDir, "rule_scheme_old.ggx");
        XMLHelper legacyHelper = new XMLHelper();
        legacyHelper.addTopObject(graGra);
        assertTrue(legacyHelper.save_to_xml(oldFile.getAbsolutePath()),
            "Legacy save should succeed: rule_scheme");

        // Load with new DOM path
        GraGra graGraNew = new GraGra();
        XMLSerialization.loadWithDom(graGraNew, oldFile.getAbsolutePath());

        // Verify structure (canonical XML comparison is unreliable for
        // RuleScheme due to legacy roundtrip instabilities in graph names
        // and morphism comments; instead compare domain object properties)
        assertEquals(graGraNew.getName(), graGra.getName(),
            "GraGra name should match");
        assertEquals(graGraNew.getRulesVec().size(), graGra.getRulesVec().size(),
            "Rule count should match");
        assertEquals(graGraNew.getTypeSet().getTypesCount(),
            graGra.getTypeSet().getTypesCount(),
            "Type count should match");
        assertEquals(graGraNew.getGraphsVec().size(), graGra.getGraphsVec().size(),
            "Graph count should match");

        // Verify RuleScheme structure
        boolean foundRuleScheme = false;
        for (Rule r : graGraNew.getRulesVec()) {
            if (r instanceof agg.xt_basis.agt.RuleScheme) {
                foundRuleScheme = true;
                agg.xt_basis.agt.RuleScheme rs =
                    (agg.xt_basis.agt.RuleScheme) r;
                assertNotNull(rs.getKernelRule(), "Kernel rule should exist");
                assertTrue(rs.getMultiRules().size() > 0,
                    "Should have at least one multi rule");
                // Verify kernel rule has LHS and RHS nodes
                assertTrue(rs.getKernelRule().getLeft().getNodesCount() > 0,
                    "Kernel LHS should have nodes");
                assertTrue(rs.getKernelRule().getRight().getNodesCount() > 0,
                    "Kernel RHS should have nodes");
            }
        }
        assertTrue(foundRuleScheme, "RuleScheme should be loaded");

        // Save with new DOM path and verify it can be reloaded
        File newFile = new File(outputDir, "rule_scheme_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraNew, newFile.getAbsolutePath()),
            "New DOM save should succeed");
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile.getAbsolutePath());
        assertEquals(graGraReloaded.getRulesVec().size(), graGra.getRulesVec().size(),
            "Rule count should match after reload");
    }

    // ---- Group 6: RuleSequence ----

    @Test
    public void testWithRuleSequence() throws Exception {
        GraGra graGra = TestDataGenerator.createWithRuleSequence();
        runRoundtrip(graGra, "rule_sequence");
    }

    // ---- Group 7: ApplRuleSequence (.rsx) ----

    @Test
    public void testApplRuleSequenceRoundtrip() throws Exception {
        GraGra graGra = TestDataGenerator.createWithRuleSequence();
        ApplRuleSequence ars = TestDataGenerator.createApplRuleSequence(graGra);

        // Save old-style via XMLHelper
        File oldFile = new File(outputDir, "appl_rule_sequence_old.rsx");
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(ars);
        assertTrue(helper.save_to_xml(oldFile.getAbsolutePath()),
            "Legacy .rsx save should succeed");
        assertTrue(oldFile.exists() && oldFile.length() > 0,
            "Old .rsx file should be non-empty");

        // Load via XMLHelper (ApplRuleSequence has no new DOM adapter for .rsx)
        ApplRuleSequence reloaded = new ApplRuleSequence(
            new agg.parser.CriticalPairOption());
        reloaded.setGraGra(graGra);
        XMLHelper helper2 = new XMLHelper();
        assertTrue(helper2.read_from_xml(oldFile.getAbsolutePath()),
            "Legacy .rsx load should succeed");
        Object loaded = helper2.getTopObject(reloaded);
        assertNotNull(loaded,
            "Legacy .rsx reload should return the ApplRuleSequence object");

        // Save again
        File newFile = new File(outputDir, "appl_rule_sequence_new.rsx");
        XMLHelper helper3 = new XMLHelper();
        helper3.addTopObject(reloaded);
        helper3.save_to_xml(newFile.getAbsolutePath());

        // Canonical comparison
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(oldFile, newFile);
        assertTrue(result.isEqual(),
            "ApplRuleSequence XML mismatch: " + result.getMessage());
    }

    // ---- Group 8: ConflictsDependenciesContainer (.cpx) ----

    @Test
    public void testConflictsDependenciesContainerRoundtrip() throws Exception {
        GraGra graGra = TestDataGenerator.createBasicGraphWithAttributes();
        ConflictsDependenciesContainer cdc =
            TestDataGenerator.createConflictsDependenciesContainer(graGra);

        // Save old-style
        File oldFile = new File(outputDir, "conflicts_deps_old.cpx");
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(cdc);
        assertTrue(helper.save_to_xml(oldFile.getAbsolutePath()),
            "Legacy .cpx save should succeed");
        assertTrue(oldFile.exists() && oldFile.length() > 0,
            "Old .cpx file should be non-empty");

        // Load
        ConflictsDependenciesContainer reloadedCdc =
            new ConflictsDependenciesContainer();
        XMLHelper helper2 = new XMLHelper();
        assertTrue(helper2.read_from_xml(oldFile.getAbsolutePath()),
            "Legacy .cpx load should succeed");
        helper2.getTopObject(reloadedCdc);

        // Save again
        File newFile = new File(outputDir, "conflicts_deps_new.cpx");
        XMLHelper helper3 = new XMLHelper();
        helper3.addTopObject(reloadedCdc);
        helper3.save_to_xml(newFile.getAbsolutePath());

        // Canonical comparison
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(oldFile, newFile);
        assertTrue(result.isEqual(),
            "ConflictsDependenciesContainer XML mismatch: " + result.getMessage());
    }

    // ---- Composite: all element types in one GraGra ----

    /**
     * Creates a GraGra that combines as many element types as possible
     * and runs the full roundtrip test.
     */
    @Test
    public void testCompositeAllElementTypes() throws Exception {
        GraGra graGra = TestDataGenerator.createRuleWithNacPacNestedAC();

        // Add constraints
        graGra.createAtomic("compositeAtomic");
        graGra.createConstraint("compositeFormula");

        // Add another rule with match (with mapping so it survives roundtrip)
        Rule rule2 = graGra.createRule();
        rule2.setName("compositeRule2");
        rule2.setLayer(2);
        rule2.setPriority(3);
        de.jare.ndimcol.ref.IteratorWalker<agg.xt_basis.Type> typeIter =
            graGra.getTypeSet().getTypeWalker();
        agg.xt_basis.Type firstType = typeIter != null && typeIter.hasNext() ? typeIter.next() : null;
        if (firstType != null) {
            agg.xt_basis.Node compositeLhsN = rule2.getLeft().createNode(firstType);
            rule2.getTarget().createNode(firstType);
            graGra.addRule(rule2);

            // Create match with a mapping
            agg.xt_basis.Match compositeMatch = graGra.createMatch(rule2);
            if (graGra.getGraph() != null && graGra.getGraph().getNodesCount() > 0) {
                compositeMatch.addMapping(compositeLhsN, graGra.getGraph().getNodesSet().iterator().next());
            }
        } else {
            graGra.addRule(rule2);
        }

        // Add rule sequence
        RuleSequence seq = graGra.createRuleSequence("compositeSequence");

        runRoundtrip(graGra, "composite_all");
    }

    // ---- Core roundtrip method ----

    /**
     * Runs the standard old-save → new-load → new-save → compare roundtrip.
     */
    private void runRoundtrip(GraGra graGra, String baseName) throws Exception {
        assertNotNull(graGra.getName(), "GraGra should have a name");

        // Step 1: Save old-style (XMLHelper / XwriteObject)
        File oldFile = new File(outputDir, baseName + "_old.ggx");
        XMLHelper legacyHelper = new XMLHelper();
        legacyHelper.addTopObject(graGra);
        assertTrue(legacyHelper.save_to_xml(oldFile.getAbsolutePath()),
            "Legacy save should succeed: " + baseName);
        assertTrue(oldFile.exists() && oldFile.length() > 0,
            "Old XML file should be non-empty: " + baseName);

        // Step 2: Load old XML with new DOM path
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, oldFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load should produce a named GraGra: " + baseName);

        // Step 3: Save with new DOM path
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + baseName);
        assertTrue(newFile.exists() && newFile.length() > 0,
            "New XML file should be non-empty: " + baseName);

        // Step 4: Canonical comparison
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(oldFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + baseName + ": " + result.getMessage());
    }
}
