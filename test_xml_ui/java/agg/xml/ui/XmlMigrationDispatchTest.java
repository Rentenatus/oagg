/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdGraGra;
import agg.gui.saveload.XmlMigrationDispatch;
import agg.parser.CriticalPairOption;
import agg.ruleappl.ApplRuleSequence;
import agg.xt_basis.GraGra;
import agg.xml.validation.XmlCanonicalComparator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Tests the real reflection wiring the GUI uses:
 * {@code agg.gui.saveload.XmlMigrationDispatch} (agg-ui) reflects into
 * {@link UIMigration} (agg-ui-xml) and must behave exactly like the
 * facade - the same contract the GraGraSave/GraGraLoad entry points rely
 * on when the migration flag is enabled.
 */
public class XmlMigrationDispatchTest {

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

    /**
     * The dispatch reflects the flag state without caching it.
     */
    @Test
    public void testDispatchReflectsFlag() {
        UIMigration.setUseNewXml(true);
        assertTrue(XmlMigrationDispatch.isUseNewXml(),
            "The dispatch should see the enabled flag");
        UIMigration.setUseNewXml(false);
        assertFalse(XmlMigrationDispatch.isUseNewXml(),
            "The dispatch should see the disabled flag immediately");
    }

    /**
     * The EdGraGra dispatch loads the frozen UI reference and saves it
     * canonically identically to the raw reference - the .ggx contract of
     * GraGraSave/GraGraLoad.
     */
    @Test
    public void testEdGraGraDispatchRoundtrip() throws Exception {
        UIMigration.setUseNewXml(true);
        File refFile = new File(PREP_DIR, "ui_basic_graph_attrs.ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen UI reference missing (run LegacyPreparationTest first): "
                + refFile.getPath());

        EdGraGra loaded = XmlMigrationDispatch.dispatchLoad(refFile.getPath());
        assertNotNull(loaded, "The dispatch load should return an EdGraGra");
        assertEquals(loaded.getBasisGraGra().getName(), "BasicGraphTest",
            "The dispatched load should keep the grammar name");

        File outFile = new File(outputDir, "ui_dispatch_basic_new.ggx");
        assertTrue(XmlMigrationDispatch.dispatchSave(loaded, outFile.getPath()),
            "The dispatch save should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "The dispatched save differs from the raw reference: "
                + result.getMessage());
    }

    /**
     * The core GraGra dispatch loads the generated reference and saves it
     * canonically identically - the .ggx contract of
     * GraGraSave.saveBase/GraGraLoad.reloadBase.
     */
    @Test
    public void testCoreGraGraDispatchRoundtrip() throws Exception {
        UIMigration.setUseNewXml(true);
        File refFile = new File(PREP_DIR, "gen_basic_graph_attrs.ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Generated reference missing (run LegacyPreparationTest first): "
                + refFile.getPath());

        GraGra graGra = new GraGra();
        assertTrue(XmlMigrationDispatch.dispatchLoad(graGra, refFile.getPath()),
            "The core dispatch load should succeed");
        assertEquals(graGra.getName(), "BasicGraphTest",
            "The dispatched core load should keep the grammar name");

        File outFile = new File(outputDir, "ui_dispatch_core_new.ggx");
        assertTrue(XmlMigrationDispatch.dispatchSave(graGra, outFile.getPath()),
            "The core dispatch save should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, outFile);
        assertTrue(result.isEqual(),
            "The dispatched core save differs from the reference: "
                + result.getMessage());
    }

    /**
     * With the flag off the dispatch still works through the facade's
     * legacy branch (the GUI calls it only when the flag is on, but the
     * facade must stay symmetric).
     */
    @Test
    public void testEdGraGraDispatchLegacyBranch() throws Exception {
        UIMigration.setUseNewXml(false);
        File refFile = new File(PREP_DIR, "ui_basic_graph_attrs.ggx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen UI reference missing: " + refFile.getPath());

        EdGraGra loaded = XmlMigrationDispatch.dispatchLoad(refFile.getPath());
        assertNotNull(loaded, "The legacy dispatch load should return an EdGraGra");
        assertEquals(loaded.getBasisGraGra().getName(), "BasicGraphTest",
            "The legacy dispatched load should keep the grammar name");
    }


    /**
     * The rule sequence dispatch loads the GUI reference (with the
     * editor layer) and saves it canonically identically - the .rsx
     * contract of ApplRuleSequenceSaveLoad.
     */
    @Test
    public void testRuleSequenceDispatchRoundtrip() throws Exception {
        UIMigration.setUseNewXml(true);
        File refFile = new File(PREP_DIR, "ui_appl_rule_sequence.rsx");
        assertTrue(refFile.exists() && refFile.length() > 0,
            "GUI .rsx reference missing (run LegacyPreparationTest first): "
                + refFile.getPath());

        ApplRuleSequence ars = new ApplRuleSequence(new CriticalPairOption());
        EdGraGra ed = XmlMigrationDispatch.dispatchLoad(ars, refFile.getPath());
        assertNotNull(ed, "The rule sequence dispatch load should return an EdGraGra");
        assertEquals(ars.getGraGra().getName(), "RuleSequenceTest",
            "The dispatched load should keep the grammar name");

        assertTrue(!ars.getRuleSequences().isEmpty(),
            "The rule sequences should be bound into the container");
        File outFile = new File(outputDir, "ui_dispatch_rsx_new.rsx");
        assertTrue(XmlMigrationDispatch.dispatchSave(ars, ed, outFile.getPath()),
            "The rule sequence dispatch save should succeed");
        // no canonical comparison against a legacy reference is possible
        // (the raw one misses the reader-side sequence binding, the
        // re-saved one carries the legacy LayoutNode.frozen derivation);
        // the DOM dispatch is verified through stability instead
        ApplRuleSequence reloadedArs = new ApplRuleSequence(new CriticalPairOption());
        EdGraGra reloaded = XmlMigrationDispatch.dispatchLoad(reloadedArs, outFile.getPath());
        assertNotNull(reloaded, "The dispatched save should be reloadable");
        File stableFile = new File(outputDir, "ui_dispatch_rsx_stable.rsx");
        assertTrue(XmlMigrationDispatch.dispatchSave(reloadedArs, reloaded, stableFile.getPath()),
            "The rule sequence dispatch stability save should succeed");
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(outFile, stableFile);
        assertTrue(result.isEqual(),
            "The dispatched rule sequence path should be stable: "
                + result.getMessage());
    }}
