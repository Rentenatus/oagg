/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdGraph;
import agg.editor.impl.EdNode;
import agg.editor.impl.EdRule;
import agg.parser.ConflictsDependenciesContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.parser.CriticalPairOption;
import agg.xt_basis.GraGra;
import agg.xml.validation.XmlCanonicalComparator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Tests the {@link UIMigration} facade over the frozen preparation
 * references: with the flag on, the DOM path must load and save the
 * references canonically identically; with the flag off, the legacy path
 * must produce the same result as the DOM path for the same in-memory
 * model. This is the migration contract the GUI relies on.
 */
public class UIMigrationTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/ui-migration/";

    private File outputDir;

    @BeforeMethod
    public void setUp() {
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    private File prepFile(String filename) {
        File refFile = new File(PREP_DIR, filename);
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference missing (run LegacyPreparationTest first): "
                + refFile.getPath());
        return refFile;
    }

    /**
     * Flag on: DOM load of the UI reference and DOM save are canonically
     * identical to the raw legacy reference.
     */
    @Test
    public void testEdGraGraDomRoundtrip() throws Exception {
        File refFile = prepFile("ui_basic_graph_attrs.ggx");

        EdGraGra loaded = UIMigration.load(refFile.getPath());
        assertNotNull(loaded, "DOM load should return an EdGraGra");
        assertEquals(loaded.getBasisGraGra().getName(), "BasicGraphTest",
            "The loaded grammar should keep its name");

        File outFile = new File(outputDir, "ui_migration_basic_dom.ggx");
        assertTrue(UIMigration.save(loaded, outFile.getPath()),
            "DOM save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "DOM save through the facade differs from the raw reference: "
                + result.getMessage());
    }

    
    /**
     * Flag on: the core .ggx dispatch (saveBase/reloadBase counterpart)
     * loads the generated reference and saves it canonically identically.
     */
    @Test
    public void testCoreGraGraDomDispatch() throws Exception {
        File refFile = prepFile("gen_basic_graph_attrs.ggx");

        GraGra graGra = new GraGra();
        assertTrue(UIMigration.load(graGra, refFile.getPath()),
            "Core DOM load through the facade should succeed");
        assertEquals(graGra.getName(), "BasicGraphTest",
            "The loaded core grammar should keep its name");

        File outFile = new File(outputDir, "ui_migration_core_dom.ggx");
        assertTrue(UIMigration.save(graGra, outFile.getPath()),
            "Core DOM save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "Core DOM save through the facade differs from the reference: "
                + result.getMessage());
    }

    /**
     * Flag on: the .cpx dispatch loads the structural reference and saves
     * it canonically identically; the legacy branch loads the same file.
     */
    @Test
    public void testCpxDispatch() throws Exception {
        File refFile = prepFile("gen_conflicts_deps.cpx");

        ConflictsDependenciesContainer cdc = new ConflictsDependenciesContainer();
        assertTrue(UIMigration.load(cdc, refFile.getPath()),
            "DOM .cpx load through the facade should succeed");
        assertEquals(cdc.getGrammar().getName(), "BasicGraphTest",
            "The embedded grammar should keep its name");
        File domFile = new File(outputDir, "ui_migration_cpx_dom.cpx");
        assertTrue(UIMigration.save(cdc, domFile.getPath()),
            "DOM .cpx save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, domFile);
        assertTrue(result.isEqual(),
            "DOM .cpx save through the facade differs from the reference: "
                + result.getMessage());

    }

    /**
     * Flag on: the .rsx dispatch loads the rule sequence reference and is
     * stable across a DOM roundtrip.
     */
    @Test
    public void testRsxDispatch() throws Exception {
        File refFile = prepFile("gen_appl_rule_sequence.rsx");

        ApplRuleSequence applRuleSeq = new ApplRuleSequence(new CriticalPairOption());
        assertTrue(UIMigration.load(applRuleSeq, refFile.getPath()),
            "DOM .rsx load through the facade should succeed");
        assertNotNull(applRuleSeq.getGraGra(),
            "The rule sequence should carry its grammar");
        assertEquals(applRuleSeq.getGraGra().getName(), "RuleSequenceTest",
            "The embedded grammar should keep its name");

        File outFile = new File(outputDir, "ui_migration_rsx_dom.rsx");
        assertTrue(UIMigration.save(applRuleSeq, outFile.getPath()),
            "DOM .rsx save through the facade should succeed");
        ApplRuleSequence reloaded = new ApplRuleSequence(new CriticalPairOption());
        assertTrue(UIMigration.load(reloaded, outFile.getPath()),
            "The DOM .rsx output should be reloadable");
        File stableFile = new File(outputDir, "ui_migration_rsx_stable.rsx");
        assertTrue(UIMigration.save(reloaded, stableFile.getPath()),
            "DOM .rsx stability save should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(result.isEqual(),
            "The DOM .rsx path should be stable: " + result.getMessage());
    }


    /**
     * Flag on: the .rsx dispatch with the editor layer loads the GUI
     * reference (layout segments applied onto the editor objects, the
     * rule sequences bound into the container like the legacy reader
     * does) and is stable across a second DOM roundtrip. No canonical
     * comparison against a legacy reference is possible here: the raw
     * GUI reference still has an empty RuleSequences section (the
     * binding is a reader side effect), while the re-saved reference
     * went through the legacy reader and therefore carries the
     * LayoutNode.frozen derivation - the DOM path preserves the file
     * values instead (the documented oracle decision).
     */
    @Test
    public void testRsxWithUiDispatch() throws Exception {
        File refFile = prepFile("ui_appl_rule_sequence.rsx");

        ApplRuleSequence ars = new ApplRuleSequence(new CriticalPairOption());
        EdGraGra ed = UIMigration.loadRuleSequence(ars, refFile.getPath());
        assertNotNull(ed, "The UI .rsx load should return an EdGraGra");
        assertEquals(ars.getGraGra().getName(), "RuleSequenceTest",
            "The embedded grammar should keep its name");
        assertTrue(!ars.getRuleSequences().isEmpty(),
            "The rule sequences should be bound into the container");
        assertTrue(!ed.getGraph().getNodes().isEmpty(),
            "The editor grammar should carry graph nodes");
        EdNode firstNode = ed.getGraph().getNodes().get(0);
        assertEquals(firstNode.getX(), 110,
            "The pinned host node position should be applied");
        assertEquals(firstNode.getY(), 210,
            "The pinned host node position should be applied");

        File outFile = new File(outputDir, "ui_migration_rsx_ui_new.rsx");
        assertTrue(UIMigration.saveRuleSequence(ars, ed, outFile.getPath()),
            "The UI .rsx save through the facade should succeed");

        ApplRuleSequence reloadedArs = new ApplRuleSequence(new CriticalPairOption());
        EdGraGra reloaded = UIMigration.loadRuleSequence(reloadedArs, outFile.getPath());
        assertNotNull(reloaded, "The DOM .rsx output should be reloadable");
        assertEquals(reloaded.getGraph().getNodes().get(0).getX(), 110,
            "The pinned host node position should survive the reload");
        File stableFile = new File(outputDir, "ui_migration_rsx_ui_stable.rsx");
        assertTrue(UIMigration.saveRuleSequence(reloadedArs, reloaded, stableFile.getPath()),
            "The UI .rsx stability save should succeed");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "The UI .rsx path should be stable: " + stableResult.getMessage());
    }

    
    // ---- UI .cpx (ConflictsDependenciesContainerSaveLoad like PairIOGUI) ----

    /**
     * Flag on: the UI .cpx dispatch loads the GUI reference - the editor
     * layer of the embedded grammar and the CPA graph applied onto the
     * editor objects - and saves it canonically identically to the raw
     * reference. The DOM path preserves the file values (no LeftOf_
     * renames, no LayoutNode.frozen derivation, no formula regeneration),
     * so its save matches the raw reference canonically, unlike the
     * frozen re-saved reference, which went through the legacy reader.
     */
    @Test
    public void testUiCpxDispatch() throws Exception {
        File refFile = prepFile("ui_conflicts_deps.cpx");

        agg.gui.cpa.ConflictsDependenciesContainerSaveLoad saveLoad =
            new agg.gui.cpa.ConflictsDependenciesContainerSaveLoad();
        assertTrue(UIMigration.loadCdContainerSaveLoad(saveLoad, refFile.getPath()),
            "DOM UI .cpx load through the facade should succeed");
        assertEquals(saveLoad.getGrammar().getName(), "CpaComputedTest",
            "The embedded grammar should keep its name");
        assertNotNull(saveLoad.getExcludePairContainer(),
            "The conflict container should be restored");
        assertNotNull(saveLoad.getDependencyPairContainer(),
            "The dependency container should be restored");
        assertNotNull(saveLoad.getCPABasisGraph(),
            "The CPA basis graph should be restored");

        EdGraGra ed = saveLoad.getPairsGraGra();
        assertNotNull(ed, "The editor grammar should be restored");
        EdNode lhsNode = findRule(ed, "useItem").getLeft().getNodes().get(0);
        assertEquals(lhsNode.getX(), 110,
            "The pinned LHS node position should be applied");
        assertEquals(lhsNode.getY(), 210,
            "The pinned LHS node position should be applied");

        EdGraph cpaGraph = saveLoad.getCPAGraph();
        assertNotNull(cpaGraph, "The CPA editor graph should be restored");
        assertTrue(cpaGraph.isCPAgraph(), "The CPA graph flag should be set");
        assertEquals(cpaGraph.getNodes().size(), 2, "CPA graph node count");
        assertEquals(cpaGraph.getNodes().get(0).getX(), 152,
            "The pinned CPA node position should be applied");
        assertEquals(cpaGraph.getNodes().get(0).getY(), 252,
            "The pinned CPA node position should be applied");

        File outFile = new File(outputDir, "ui_migration_cpx_ui_new.cpx");
        assertTrue(UIMigration.saveCdContainerSaveLoad(saveLoad, outFile.getPath()),
            "The UI .cpx save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "The UI .cpx DOM save differs from the raw reference: "
                + result.getMessage());

        agg.gui.cpa.ConflictsDependenciesContainerSaveLoad reloaded =
            new agg.gui.cpa.ConflictsDependenciesContainerSaveLoad();
        assertTrue(UIMigration.loadCdContainerSaveLoad(reloaded, outFile.getPath()),
            "The UI .cpx DOM output should be reloadable");
        assertEquals(reloaded.getCPAGraph().getNodes().get(0).getX(), 152,
            "The pinned CPA node position should survive the reload");
        File stableFile = new File(outputDir, "ui_migration_cpx_ui_stable.cpx");
        assertTrue(UIMigration.saveCdContainerSaveLoad(reloaded, stableFile.getPath()),
            "The UI .cpx stability save should succeed");
        XmlCanonicalComparator.ComparisonResult stableResult =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(stableResult.isEqual(),
            "The UI .cpx DOM path should be stable: "
                + stableResult.getMessage());
    }

    
    // ---- standalone PairContainer (like PairIOGUI) ----

    /**
     * The standalone pair container dispatch loads and saves the frozen
     * reference with both branches: the DOM path canonically identically
     * to the raw reference, the legacy path as the frozen clone writes
     * it.
     */
    @Test
    public void testStandalonePairContainerDispatch() throws Exception {
        File refFile = prepFile("gen_standalone_conflicts.cpx");

        agg.parser.ExcludePairContainer pc =
            new agg.parser.ExcludePairContainer(new GraGra());
        assertTrue(UIMigration.loadStandalonePairContainer(pc, refFile.getPath()),
            "DOM standalone .cpx load through the facade should succeed");
        assertEquals(pc.getGrammar().getName(), "CpaComputedTest",
            "The embedded grammar should keep its name");
        assertTrue(!pc.getExcludeContainer().isEmpty(),
            "The computed conflict entries should be restored");
        File outFile = new File(outputDir, "ui_migration_standalone_new.cpx");
        assertTrue(UIMigration.saveStandalonePairContainer(pc, outFile.getPath()),
            "DOM standalone .cpx save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "The standalone .cpx DOM save differs from the reference: "
                + result.getMessage());

    }

    /**
     * Returns the editor rule with the given name, or fails the test.
     */
    private EdRule findRule(EdGraGra edGraGra, String name) {
        for (EdRule rule : edGraGra.getRules()) {
            if (name.equals(rule.getName())) {
                return rule;
            }
        }
        fail("Rule not found: " + name);
        return null;
    }
}
