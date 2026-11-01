/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdGraGra;
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
}
