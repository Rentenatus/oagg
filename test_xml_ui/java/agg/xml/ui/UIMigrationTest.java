/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdNode;
import agg.parser.ConflictsDependenciesContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.parser.CriticalPairOption;
import agg.xt_basis.GraGra;
import agg.xml.validation.XmlCanonicalComparator;
import org.testng.annotations.AfterMethod;
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
    private boolean savedFlag;

    @BeforeMethod
    public void setUp() {
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        savedFlag = UIMigration.isUseNewXml();
    }

    @AfterMethod
    public void tearDown() {
        UIMigration.setUseNewXml(savedFlag);
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
        UIMigration.setUseNewXml(true);
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
     * Flag off: the legacy path of the facade loads the UI reference and
     * saves canonically identically to the frozen legacy re-save
     * reference (the legacy reader derives LayoutNode.frozen from age,
     * so the legacy branch is compared against the legacy oracle, not
     * against the raw reference).
     */
    @Test
    public void testEdGraGraLegacyAgreesWithDom() throws Exception {
        File refFile = prepFile("ui_basic_graph_attrs.ggx");
        File resavedFile = prepFile("ui_basic_graph_attrs_resaved.ggx");

        UIMigration.setUseNewXml(false);
        EdGraGra legacyLoaded = UIMigration.load(refFile.getPath());
        assertNotNull(legacyLoaded, "Legacy load should return an EdGraGra");
        assertEquals(legacyLoaded.getBasisGraGra().getName(), "BasicGraphTest",
            "The legacy loaded grammar should keep its name");
        File legacyFile = new File(outputDir, "ui_migration_basic_legacy.ggx");
        assertTrue(UIMigration.save(legacyLoaded, legacyFile.getPath()),
            "Legacy save through the facade should succeed");
        XmlCanonicalComparator.ComparisonResult legacyResult =
            XmlCanonicalComparator.compareFiles(resavedFile, legacyFile);
        assertTrue(legacyResult.isEqual(),
            "Legacy save through the facade differs from the frozen legacy"
                + " re-save reference: " + legacyResult.getMessage());

        // the DOM save of the same file stays canonically identical to the
        // raw reference (covered by testEdGraGraDomRoundtrip); both paths
        // agree on the model structure
        UIMigration.setUseNewXml(true);
        EdGraGra domLoaded = UIMigration.load(refFile.getPath());
        assertNotNull(domLoaded, "DOM load should return an EdGraGra");
        assertEquals(domLoaded.getRules().size(), legacyLoaded.getRules().size(),
            "Both paths should load the same rule count");
        assertEquals(domLoaded.getGraphs().size(), legacyLoaded.getGraphs().size(),
            "Both paths should load the same graph count");
    }

    /**
     * Flag on: the core .ggx dispatch (saveBase/reloadBase counterpart)
     * loads the generated reference and saves it canonically identically.
     */
    @Test
    public void testCoreGraGraDomDispatch() throws Exception {
        UIMigration.setUseNewXml(true);
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

        UIMigration.setUseNewXml(true);
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

        UIMigration.setUseNewXml(false);
        ConflictsDependenciesContainer legacyCdc = new ConflictsDependenciesContainer();
        assertTrue(UIMigration.load(legacyCdc, refFile.getPath()),
            "Legacy .cpx load through the facade should succeed");
        assertEquals(legacyCdc.getGrammar().getName(), "BasicGraphTest",
            "The legacy loaded embedded grammar should keep its name");
    }

    /**
     * Flag on: the .rsx dispatch loads the rule sequence reference and is
     * stable across a DOM roundtrip.
     */
    @Test
    public void testRsxDispatch() throws Exception {
        UIMigration.setUseNewXml(true);
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
        UIMigration.setUseNewXml(true);
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

    /**
     * Flag off: the legacy branch of the .rsx facade loads the GUI
     * reference (editor layer via enrichObject) and its save is
     * canonically identical to the frozen re-saved reference - both
     * sides come from the legacy-loaded state.
     */
    @Test
    public void testRsxWithUiLegacyBranch() throws Exception {
        UIMigration.setUseNewXml(false);
        File refFile = prepFile("ui_appl_rule_sequence.rsx");
        File resavedFile = prepFile("ui_appl_rule_sequence_resaved.rsx");

        ApplRuleSequence ars = new ApplRuleSequence(new CriticalPairOption());
        EdGraGra ed = UIMigration.loadRuleSequence(ars, refFile.getPath());
        assertNotNull(ed, "The legacy UI .rsx load should return an EdGraGra");
        assertEquals(ars.getGraGra().getName(), "RuleSequenceTest",
            "The embedded grammar should keep its name");
        assertEquals(ed.getGraph().getNodes().get(0).getX(), 110,
            "The pinned host node position should be applied by enrichObject");

        File outFile = new File(outputDir, "ui_migration_rsx_ui_legacy.rsx");
        assertTrue(UIMigration.saveRuleSequence(ars, ed, outFile.getPath()),
            "The legacy UI .rsx save should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(resavedFile, outFile);
        assertTrue(result.isEqual(),
            "The legacy UI .rsx save differs from the frozen re-saved"
                + " reference: " + result.getMessage());
    }
}
