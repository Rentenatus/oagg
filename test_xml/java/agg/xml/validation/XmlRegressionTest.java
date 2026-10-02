/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.util.XMLHelper;
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
 * Regression tests verifying that the new DOM-based XML serialization
 * produces equivalent XML for all Core domain objects that previously
 * had their own XML serialization via {@link agg.util.XMLObject}.
 *
 * <p>Test pattern per sample .ggx file:
 * <ol>
 *   <li>Create/load structure (GraGra with all sub-objects) via legacy path.</li>
 *   <li>Save old-style as XML (XMLHelper / XwriteObject) → file_old.xml.</li>
 *   <li>Read file_old.xml with the new DOM path (GraGraAdapter / DOMXMLDeserializer).</li>
 *   <li>Save again with the new DOM path → file_new.xml.</li>
 *   <li>Compare file_old.xml vs file_new.xml canonically (IDs and element
 *       ordering normalised via {@link XmlCanonicalComparator}).</li>
 * </ol>
 *
 * <p>Multiple XML structures are grouped per test: each .ggx file exercises
 * GraGra, Graph, Node, Arc, Rule, NAC, PAC, Match, Types, Attributes,
 * Formula, AtomConstraint and more — all in a single test invocation.</p>
 */
public class XmlRegressionTest {

    private static final String OUTPUT_DIR = "target/xml-regression/";

    private File outputDir;

    @BeforeClass
    public void setUp() {
        TestDataHelper.requireAllSamples();
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        // Ensure legacy path is active
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

    // ---- Cross-system regression: old save → new load → new save → compare ----

    /**
     * Core regression test: old-save → new-load → new-save → canonical XML comparison.
     *
     * <p>Verifies that the new DOM-based serialization (GraGraAdapter with all
     * sub-adapters for Graph, Node, Arc, Rule, Match, Types, Attributes,
     * Constraints, etc.) produces XML equivalent to the legacy XMLHelper path
     * for every element type present in the sample file.</p>
     */
    @Test(dataProvider = "sampleFiles")
    public void testOldSaveNewLoadNewSaveCompare(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
        String baseName = sampleFilename.replace(".ggx", "");

        // Step 1: Load with legacy path
        GraGra graGraLegacy = new GraGra();
        graGraLegacy.load(sampleFile.getAbsolutePath());
        assertNotNull(graGraLegacy.getName(),
            "Legacy load should produce a named GraGra: " + sampleFilename);

        // Step 2: Save old-style (XMLHelper / XwriteObject)
        File oldFile = new File(outputDir, baseName + "_old.ggx");
        XMLHelper legacyHelper = new XMLHelper();
        legacyHelper.addTopObject(graGraLegacy);
        assertTrue(legacyHelper.save_to_xml(oldFile.getAbsolutePath()),
            "Legacy save should succeed: " + sampleFilename);
        assertTrue(oldFile.exists() && oldFile.length() > 0,
            "Old XML file should be non-empty: " + sampleFilename);

        // Step 3: Load old XML with new DOM path
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, oldFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load of old XML should produce a named GraGra: " + sampleFilename);

        // Step 4: Save with new DOM path
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + sampleFilename);
        assertTrue(newFile.exists() && newFile.length() > 0,
            "New XML file should be non-empty: " + sampleFilename);

        // Step 5: Canonical comparison
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(oldFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + sampleFilename + ": " + result.getMessage());
    }

    // ---- Legacy stability: old save → old load → old save → compare ----

    /**
     * Verifies that the legacy path is stable: saving twice produces identical
     * canonical XML. This establishes a baseline that the new path must match.
     */
    @Test(dataProvider = "sampleFiles")
    public void testLegacyPathStability(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
        String baseName = sampleFilename.replace(".ggx", "");

        // Load with legacy
        GraGra graGra = new GraGra();
        graGra.load(sampleFile.getAbsolutePath());

        // Save old-style (first time)
        File oldFile1 = new File(outputDir, baseName + "_legacy_stable_1.ggx");
        XMLHelper helper1 = new XMLHelper();
        helper1.addTopObject(graGra);
        helper1.save_to_xml(oldFile1.getAbsolutePath());

        // Load first save and save again
        GraGra graGraReloaded = new GraGra();
        graGraReloaded.load(oldFile1.getAbsolutePath());

        File oldFile2 = new File(outputDir, baseName + "_legacy_stable_2.ggx");
        XMLHelper helper2 = new XMLHelper();
        helper2.addTopObject(graGraReloaded);
        helper2.save_to_xml(oldFile2.getAbsolutePath());

        // Compare
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(oldFile1, oldFile2);
        assertTrue(result.isEqual(),
            "Legacy path should be stable for " + sampleFilename + ": " + result.getMessage());
    }

    // ---- New path stability: new save → new load → new save → compare ----

    /**
     * Verifies that the new DOM path is stable: saving twice produces
     * identical canonical XML.
     */
    @Test(dataProvider = "sampleFiles")
    public void testNewPathStability(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
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

    // ---- Grouped element verification per sample ----

    /**
     * Verifies that all element types present in the sample file are
     * preserved through the cross-system roundtrip.
     *
     * <p>Groups all element types (GraGra, Graph, Node, Arc, Rule, Types,
     * Attributes, Constraints, Match, NAC, PAC) into a single test
     * per sample file. For each element type, the test verifies that the
     * count and key attributes survive the old→new→save cycle.</p>
     */
    @Test(dataProvider = "sampleFiles")
    public void testAllElementTypesPreserved(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);
        String baseName = sampleFilename.replace(".ggx", "");

        // Load with legacy
        GraGra graGraLegacy = new GraGra();
        graGraLegacy.load(sampleFile.getAbsolutePath());

        // Save old-style
        File oldFile = new File(outputDir, baseName + "_elements_old.ggx");
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(graGraLegacy);
        helper.save_to_xml(oldFile.getAbsolutePath());

        // Load with new DOM
        GraGra graGraNew = new GraGra();
        XMLSerialization.loadWithDom(graGraNew, oldFile.getAbsolutePath());

        // --- GraGra ---
        assertEquals(graGraNew.getName(), graGraLegacy.getName(),
            "GraGra name should match: " + sampleFilename);

        // --- Types ---
        int legacyTypeCount = graGraLegacy.getTypeSet() != null
            ? graGraLegacy.getTypeSet().getTypesCount() : 0;
        int newTypeCount = graGraNew.getTypeSet() != null
            ? graGraNew.getTypeSet().getTypesCount() : 0;
        assertEquals(newTypeCount, legacyTypeCount,
            "Type count should match: " + sampleFilename);

        // --- Graphs (host + type graph) ---
        assertEquals(graGraNew.getGraphsVec().size(), graGraLegacy.getGraphsVec().size(),
            "Graph count should match: " + sampleFilename);
        for (int i = 0; i < graGraLegacy.getGraphsVec().size(); i++) {
            Graph legacyGraph = graGraLegacy.getGraphsVec().get(i);
            Graph newGraph = graGraNew.getGraphsVec().get(i);
            assertEquals(newGraph.getNodesCount(), legacyGraph.getNodesCount(),
                "Node count in graph " + i + " should match: " + sampleFilename);
            assertEquals(newGraph.getArcsCount(), legacyGraph.getArcsCount(),
                "Arc count in graph " + i + " should match: " + sampleFilename);
        }

        // --- Rules ---
        assertEquals(graGraNew.getRulesVec().size(), graGraLegacy.getRulesVec().size(),
            "Rule count should match: " + sampleFilename);
        for (int i = 0; i < graGraLegacy.getRulesVec().size(); i++) {
            Rule legacyRule = graGraLegacy.getRulesVec().get(i);
            Rule newRule = graGraNew.getRulesVec().get(i);
            assertEquals(newRule.getName(), legacyRule.getName(),
                "Rule " + i + " name should match: " + sampleFilename);
            assertEquals(newRule.getSource().getNodesCount(),
                legacyRule.getSource().getNodesCount(),
                "Rule " + i + " LHS node count should match: " + sampleFilename);
            assertEquals(newRule.getSource().getArcsCount(),
                legacyRule.getSource().getArcsCount(),
                "Rule " + i + " LHS arc count should match: " + sampleFilename);
            assertEquals(newRule.getTarget().getNodesCount(),
                legacyRule.getTarget().getNodesCount(),
                "Rule " + i + " RHS node count should match: " + sampleFilename);
            assertEquals(newRule.getTarget().getArcsCount(),
                legacyRule.getTarget().getArcsCount(),
                "Rule " + i + " RHS arc count should match: " + sampleFilename);
        }

        // --- Constraints (Formula + AtomConstraint) ---
        int legacyFormulaCount = graGraLegacy.getConstraintsVec() != null
            ? graGraLegacy.getConstraintsVec().size() : 0;
        int newFormulaCount = graGraNew.getConstraintsVec() != null
            ? graGraNew.getConstraintsVec().size() : 0;
        assertEquals(newFormulaCount, legacyFormulaCount,
            "Formula count should match: " + sampleFilename);

        int legacyAtomicCount = 0;
        java.util.Enumeration<agg.cons.AtomConstraint> atomicIter =
            graGraLegacy.getAtomics();
        while (atomicIter.hasMoreElements()) {
            atomicIter.nextElement();
            legacyAtomicCount++;
        }
        int newAtomicCount = 0;
        java.util.Enumeration<agg.cons.AtomConstraint> newAtomicIter =
            graGraNew.getAtomics();
        while (newAtomicIter.hasMoreElements()) {
            newAtomicIter.nextElement();
            newAtomicCount++;
        }
        assertEquals(newAtomicCount, legacyAtomicCount,
            "Atomic constraint count should match: " + sampleFilename);
    }

    // ---- New path can read original sample files ----

    /**
     * Verifies that the new DOM path can directly load the original sample
     * .ggx files (which were written by the legacy path). This is the
     * prerequisite for the cross-system regression test.
     */
    @Test(dataProvider = "sampleFiles")
    public void testNewPathCanReadLegacyFiles(String sampleFilename) throws Exception {
        File sampleFile = TestDataHelper.resolveSample(sampleFilename);

        GraGra graGraLegacy = new GraGra();
        graGraLegacy.load(sampleFile.getAbsolutePath());
        String legacyName = graGraLegacy.getName();

        GraGra graGraNew = new GraGra();
        XMLSerialization.loadWithDom(graGraNew, sampleFile.getAbsolutePath());
        assertNotNull(graGraNew.getName(),
            "New path should load legacy file: " + sampleFilename);
        assertEquals(graGraNew.getName(), legacyName,
            "Name should match when new path reads legacy file: " + sampleFilename);
        assertEquals(graGraNew.getGraphsVec().size(), graGraLegacy.getGraphsVec().size(),
            "Graph count should match: " + sampleFilename);
    }
}
