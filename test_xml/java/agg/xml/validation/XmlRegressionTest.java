/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.Rule;
import agg.xml.TestDataHelper;
import agg.xml.XMLSerialization;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * DOM-side cross-system regression tests.
 *
 * <p>The legacy half (loading and saving each sample with the legacy
 * XMLHelper path) ran in the PREPARATION phase against the FROZEN legacy
 * clone (agg-core-legacy): the suite {@code agg.xml.prep.LegacyPreparationTest}
 * produced reference files {@code target/legacy_prep/sample_<name>.ggx}.
 * This class is the "actual" half running against the CURRENT code in
 * src:</p>
 * <ol>
 *   <li>DOM load of the frozen reference XML.</li>
 *   <li>DOM save.</li>
 *   <li>Canonical comparison against the frozen reference.</li>
 *   <li>Structural verification: element counts must survive the DOM
 *       roundtrip of the frozen reference content.</li>
 * </ol>
 *
 * <p>If src later deviates from the old XML behaviour, the canonical
 * comparison fails even though the frozen preparation is untouched.</p>
 */
public class XmlRegressionTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/xml-regression/";

    private File outputDir;

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        // Ensure legacy path is active for XMLSerialization-internal fallbacks
        XMLSerialization.setUseNewXml(false);
    }

    @AfterClass
    public void tearDown() {
        XMLSerialization.setUseNewXml(false);
    }

    @DataProvider(name = "sampleFiles")
    public Object[][] sampleFiles() {
        Object[][] data = new Object[TestDataHelper.SAMPLE_FILES.length][1];
        for (int i = 0; i < TestDataHelper.SAMPLE_FILES.length; i++) {
            data[i][0] = TestDataHelper.SAMPLE_FILES[i];
        }
        return data;
    }

    // ---- Cross-system regression: frozen reference -> DOM load -> DOM save -> compare ----

    /**
     * Core regression test: DOM load of the frozen legacy reference ->
     * DOM save -> canonical XML comparison against the reference.
     */
    @Test(dataProvider = "sampleFiles")
    public void testOldSaveNewLoadNewSaveCompare(String sampleFilename) throws Exception {
        File refFile = prepFile(sampleFilename);
        String baseName = sampleFilename.replace(".ggx", "");

        // Step 1: Load frozen reference with new DOM path
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, refFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load of the frozen reference should produce a named GraGra: " + sampleFilename);

        // Step 2: Save with new DOM path
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + sampleFilename);
        assertTrue(newFile.exists() && newFile.length() > 0,
            "New XML file should be non-empty: " + sampleFilename);

        // Step 3: Canonical comparison
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + sampleFilename + ": " + result.getMessage());
    }

    // ---- New path stability: new save -> new load -> new save -> compare ----

    /**
     * Verifies that the new DOM path is stable: saving twice produces
     * identical canonical XML.
     */
    @Test(dataProvider = "sampleFiles")
    public void testNewPathStability(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
        TestDataHelper.requireFile(sampleFile);
        String baseName = sampleFilename.replace(".ggx", "");

        // Load with new DOM path
        GraGra graGra = new GraGra();
        XMLSerialization.loadWithDom(graGra, sampleFile.getAbsolutePath());

        // Save new (first time)
        File newFile1 = new File(outputDir, baseName + "_dom_stable_1.ggx");
        XMLSerialization.saveWithDom(graGra, newFile1.getAbsolutePath());

        // Load first save and save again
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile1.getAbsolutePath());

        File newFile2 = new File(outputDir, baseName + "_dom_stable_2.ggx");
        XMLSerialization.saveWithDom(graGraReloaded, newFile2.getAbsolutePath());

        // Compare
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(newFile1, newFile2);
        assertTrue(result.isEqual(),
            "New DOM path should be stable for " + sampleFilename + ": " + result.getMessage());
    }

    // ---- Grouped element verification per frozen reference ----

    /**
     * Verifies that all element types present in the frozen reference
     * survive the DOM roundtrip: counts are compared between the DOM load
     * of the frozen reference and the DOM load of its DOM re-save.
     */
    @Test(dataProvider = "sampleFiles")
    public void testAllElementTypesPreserved(String sampleFilename) throws Exception {
        File refFile = prepFile(sampleFilename);

        // Load frozen reference with new DOM path
        GraGra graGraNew = new GraGra();
        XMLSerialization.loadWithDom(graGraNew, refFile.getAbsolutePath());

        // Save with new DOM path and reload
        File newFile = new File(outputDir, sampleFilename.replace(".ggx", "") + "_elements_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraNew, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + sampleFilename);
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile.getAbsolutePath());

        // --- GraGra ---
        assertEquals(graGraReloaded.getName(), graGraNew.getName(),
            "GraGra name should match: " + sampleFilename);

        // --- Types ---
        int newTypeCount = graGraNew.getTypeSet() != null
            ? graGraNew.getTypeSet().getTypesCount() : 0;
        int reloadedTypeCount = graGraReloaded.getTypeSet() != null
            ? graGraReloaded.getTypeSet().getTypesCount() : 0;
        assertEquals(reloadedTypeCount, newTypeCount,
            "Type count should match: " + sampleFilename);

        // --- Graphs (host + type graph) ---
        assertEquals(graGraReloaded.getGraphsVec().size(), graGraNew.getGraphsVec().size(),
            "Graph count should match: " + sampleFilename);
        for (int i = 0; i < graGraNew.getGraphsVec().size(); i++) {
            Graph refGraph = graGraNew.getGraphsVec().get(i);
            Graph reloadedGraph = graGraReloaded.getGraphsVec().get(i);
            assertEquals(reloadedGraph.getNodesCount(), refGraph.getNodesCount(),
                "Node count in graph " + i + " should match: " + sampleFilename);
            assertEquals(reloadedGraph.getArcsCount(), refGraph.getArcsCount(),
                "Arc count in graph " + i + " should match: " + sampleFilename);
        }

        // --- Rules ---
        assertEquals(graGraReloaded.getRulesVec().size(), graGraNew.getRulesVec().size(),
            "Rule count should match: " + sampleFilename);
        for (int i = 0; i < graGraNew.getRulesVec().size(); i++) {
            Rule refRule = graGraNew.getRulesVec().get(i);
            Rule reloadedRule = graGraReloaded.getRulesVec().get(i);
            assertEquals(reloadedRule.getName(), refRule.getName(),
                "Rule " + i + " name should match: " + sampleFilename);
            assertEquals(reloadedRule.getSource().getNodesCount(),
                refRule.getSource().getNodesCount(),
                "Rule " + i + " LHS node count should match: " + sampleFilename);
            assertEquals(reloadedRule.getSource().getArcsCount(),
                refRule.getSource().getArcsCount(),
                "Rule " + i + " LHS arc count should match: " + sampleFilename);
            assertEquals(reloadedRule.getTarget().getNodesCount(),
                refRule.getTarget().getNodesCount(),
                "Rule " + i + " RHS node count should match: " + sampleFilename);
            assertEquals(reloadedRule.getTarget().getArcsCount(),
                refRule.getTarget().getArcsCount(),
                "Rule " + i + " RHS arc count should match: " + sampleFilename);
        }

        // --- Constraints (Formula + AtomConstraint) ---
        int newFormulaCount = graGraNew.getConstraintsVec() != null
            ? graGraNew.getConstraintsVec().size() : 0;
        int reloadedFormulaCount = graGraReloaded.getConstraintsVec() != null
            ? graGraReloaded.getConstraintsVec().size() : 0;
        assertEquals(reloadedFormulaCount, newFormulaCount,
            "Formula count should match: " + sampleFilename);

        int newAtomicCount = countAtomics(graGraNew);
        int reloadedAtomicCount = countAtomics(graGraReloaded);
        assertEquals(reloadedAtomicCount, newAtomicCount,
            "Atomic constraint count should match: " + sampleFilename);
    }

    // ---- New path can read the original sample files ----

    /**
     * Verifies that the new DOM path can directly load the original sample
     * .ggx files and that the result matches the DOM load of the frozen
     * reference (the same content written by the frozen legacy clone).
     */
    @Test(dataProvider = "sampleFiles")
    public void testNewPathCanReadLegacyFiles(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
        TestDataHelper.requireFile(sampleFile);
        File refFile = prepFile(sampleFilename);

        // DOM load of the original sample file
        GraGra graGraOriginal = new GraGra();
        XMLSerialization.loadWithDom(graGraOriginal, sampleFile.getAbsolutePath());
        assertNotNull(graGraOriginal.getName(),
            "New path should load legacy file: " + sampleFilename);

        // DOM load of the frozen reference (clone-written)
        GraGra graGraFromRef = new GraGra();
        XMLSerialization.loadWithDom(graGraFromRef, refFile.getAbsolutePath());
        assertNotNull(graGraFromRef.getName(),
            "New path should load frozen reference: " + sampleFilename);

        assertEquals(graGraFromRef.getName(), graGraOriginal.getName(),
            "Name should match between original and frozen reference: " + sampleFilename);
        assertEquals(graGraFromRef.getGraphsVec().size(), graGraOriginal.getGraphsVec().size(),
            "Graph count should match between original and frozen reference: " + sampleFilename);
    }

    private int countAtomics(GraGra graGra) {
        int count = 0;
        java.util.Enumeration<agg.cons.AtomConstraint> atomics = graGra.getAtomics();
        while (atomics.hasMoreElements()) {
            atomics.nextElement();
            count++;
        }
        return count;
    }

    private File prepFile(String sampleFilename) {
        File refFile = new File(PREP_DIR, "sample_" + sampleFilename);
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference file is missing (run the legacy preparation "
                + "suite first: mvn test at the parent, or -pl test/test_agg/legacy_agg): "
                + refFile.getPath());
        return refFile;
    }
}
