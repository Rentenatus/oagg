/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.parser.ConflictsDependenciesContainer;
import agg.util.Pair;
import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xt_basis.Rule;
import agg.xml.XMLSerialization;
import agg.xml.core.XMLSerializationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Edge case tests for the .cpx DOM reader with hand-written minimal
 * files:
 * <ul>
 *   <li>the explicit rejection of old-style overlap morphisms without a
 *       source attribute (the readOldOverlappingMorphisms variant of the
 *       legacy reader),</li>
 *   <li>the legacy "Regel" element fallback for rule pair entries,
 *       cross-checked against the legacy reader of the same file.</li>
 * </ul>
 */
public class CpxEdgeCaseTest {

    private static final String OUTPUT_DIR = "target/cpx-edge-case/";

    private File outputDir;

    @BeforeClass
    public void setUp() {
        outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * Minimal .cpx preamble: a tiny grammar with one type and one rule,
     * plus the cpaOptions and the conflict container head with its rule
     * sets. The caller appends the rule pair entries and the closing tags.
     */
    private static final String MINI_CPX_PREAMBLE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<Document version=\"1.0\">\n"
        + "  <CriticalPairs ID=\"I0\">\n"
        + "    <GraphTransformationSystem ID=\"I1\" directed=\"true\""
        + " name=\"EdgeCase\" parallel=\"true\">\n"
        + "      <Types>\n"
        + "        <NodeType ID=\"I2\" abstract=\"false\""
        + " name=\"Item%:RECT:java.awt.Color[r=0,g=0,b=0]::[NODE]:\"/>\n"
        + "      </Types>\n"
        + "      <Rule ID=\"I3\" formula=\"true\" name=\"r1\">\n"
        + "        <Graph ID=\"I4\" kind=\"LHS\" name=\"Left\">\n"
        + "          <Node ID=\"I5\" type=\"I2\"/>\n"
        + "        </Graph>\n"
        + "        <Graph ID=\"I6\" kind=\"RHS\" name=\"Right\"/>\n"
        + "        <Morphism name=\"r1\"/>\n"
        + "        <TaggedValue Tag=\"layer\" TagValue=\"0\"/>\n"
        + "        <TaggedValue Tag=\"priority\" TagValue=\"0\"/>\n"
        + "      </Rule>\n"
        + "    </GraphTransformationSystem>\n"
        + "    <cpaOptions complete=\"true\" consistent=\"true\""
        + " directlyStrictConfluent=\"false\""
        + " directlyStrictConfluentUpToIso=\"false\" essential=\"false\""
        + " ignoreSameMatch=\"false\" ignoreSameRule=\"false\""
        + " maxBoundOfCriticCause=\"0\" namedObject=\"false\""
        + " strongAttrCheck=\"false\"/>\n"
        + "    <conflictContainer kind=\"exclude\">\n"
        + "      <RuleSet i0=\"I3\" size=\"1\"/>\n"
        + "      <RuleSet2 i0=\"I3\" size=\"1\"/>\n";

    private static final String MINI_CPX_EPILOGUE =
        "    </conflictContainer>\n"
        + "    <conflictFreeContainer/>\n"
        + "  </CriticalPairs>\n"
        + "</Document>\n";

    private File writeMiniCpx(String fileName, String entries) throws Exception {
        File file = new File(outputDir, fileName);
        Writer writer = new FileWriter(file);
        try {
            writer.write(MINI_CPX_PREAMBLE);
            writer.write(entries);
            writer.write(MINI_CPX_EPILOGUE);
        } finally {
            writer.close();
        }
        assertTrue(file.exists() && file.length() > 0,
            "Mini .cpx should be written: " + fileName);
        return file;
    }

    /**
     * The DOM path rejects old-style overlap morphisms without a source
     * attribute explicitly instead of silently dropping data.
     */
    @Test
    public void testOldStyleOverlapMorphismRejected() throws Exception {
        String entries =
              "      <Rule R1=\"I3\">\n"
            + "        <Rule R2=\"I3\" bool=\"true\" caIndx=\"-1:\""
            + " duIndx=\"-1:\" pfIndx=\"-1:-1:\">\n"
            + "          <Overlapping_Pair>\n"
            + "            <Graph ID=\"I7\" kind=\"GRAPH\""
            + " name=\"edge-case-overlap\">\n"
            + "              <Node ID=\"I8\" type=\"I2\"/>\n"
            + "            </Graph>\n"
            + "            <Morphism name=\"first\"/>\n"
            + "          </Overlapping_Pair>\n"
            + "        </Rule>\n"
            + "      </Rule>\n";
        File file = writeMiniCpx("oldstyle_morphism.cpx", entries);

        ConflictsDependenciesContainer cdc = new ConflictsDependenciesContainer();
        try {
            XMLSerialization.loadWithDom(cdc, file.getAbsolutePath());
            fail("Old-style overlap morphisms without a source attribute"
                + " should be rejected by the DOM path");
        } catch (XMLSerializationException expected) {
            assertTrue(expected.getMessage().contains("source"),
                "The rejection should name the missing source attribute: "
                    + expected.getMessage());
        }
    }

    /**
     * The DOM path restores rule pair entries written with the old German
     * "Regel" element tag, and the legacy reader of the same file agrees
     * on the entry and its criticality flag.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testRegelFallback() throws Exception {
        String entries =
              "      <Regel R1=\"I3\">\n"
            + "        <Regel R2=\"I3\" bool=\"false\" caIndx=\"-1:\""
            + " duIndx=\"-1:\" pfIndx=\"-1:-1:\"/>\n"
            + "      </Regel>\n";
        File file = writeMiniCpx("regel_fallback.cpx", entries);

        // DOM load restores the entry
        ConflictsDependenciesContainer cdc = new ConflictsDependenciesContainer();
        XMLSerialization.loadWithDom(cdc, file.getAbsolutePath());
        assertNotNull(cdc.getExcludePairContainer(),
            "DOM load should create the conflict container");
        assertFalse(cdc.getExcludePairContainer().getExcludeContainer().isEmpty(),
            "The Regel entry should be restored by the DOM path");
        Rule rule = findRule(cdc.getGrammar(), "r1");
        Map secondPart = cdc.getExcludePairContainer()
            .getExcludeContainer().get(rule);
        assertNotNull(secondPart, "The Regel entry should key on r1");
        Pair pair = (Pair) secondPart.get(rule);
        assertNotNull(pair, "The Regel entry should map r1 to r1");
        assertEquals(pair.first, Boolean.FALSE,
            "The Regel entry should restore the non-critical flag");

        // Legacy load of the same file agrees
        XMLHelper helper = new XMLHelper();
        assertTrue(helper.read_from_xml(file.getAbsolutePath()),
            "Legacy load of the Regel mini file should succeed");
        ConflictsDependenciesContainer legacy = new ConflictsDependenciesContainer();
        helper.getTopObject(legacy);
        assertFalse(legacy.getExcludePairContainer().getExcludeContainer().isEmpty(),
            "The legacy reader should also restore the Regel entry");
        Rule legacyRule = findRule(legacy.getGrammar(), "r1");
        Map legacySecondPart = legacy.getExcludePairContainer()
            .getExcludeContainer().get(legacyRule);
        Pair legacyPair = (Pair) legacySecondPart.get(legacyRule);
        assertEquals(legacyPair.first, Boolean.FALSE,
            "The legacy reader should restore the same flag");
    }

    private Rule findRule(GraGra gra, String name) {
        for (Rule r : gra.getListOfRules()) {
            if (name.equals(r.getName())) {
                return r;
            }
        }
        fail("Rule not found: " + name);
        return null;
    }
}
