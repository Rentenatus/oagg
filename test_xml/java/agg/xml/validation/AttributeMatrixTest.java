/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.attribute.handler.HandlerExpr;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.CondTuple;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.VarTuple;
import agg.xml.XMLSerialization;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Node;
import agg.xt_basis.Rule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

/**
 * Broad attribute coverage for the XML serialization roundtrip. AGG is
 * attribute-bearing: the attribute function spans type declarations with
 * diverse types, absolute values on nodes and edges, and formulas inside
 * morphisms (variables, expressions, conditions).
 *
 * <p>Covered dimensions:
 * <ul>
 *   <li>Absolute values for every basic type (boolean, int, long, float,
 *       double, String) including edge cases (0, negative, max, empty string,
 *       XML-relevant special characters)</li>
 *   <li>Attribute value forms inside a rule morphism mapping: constant,
 *       variable, expression and condition, on the mapped LHS/RHS node pair</li>
 *   <li>Edge attributes with variable and expression inside the mapped
 *       edge of a rule morphism</li>
 * </ul>
 *
 * <p>The scenario generation and the legacy save ran in the PREPARATION
 * phase against the FROZEN legacy clone (agg-core-legacy), producing
 * reference files {@code target/legacy_prep/attr_*.ggx}. This class is
 * the "actual" half running against the CURRENT code in src: DOM load of
 * the frozen reference, verification of the attribute value forms, DOM
 * save and canonical comparison.</p>
 */
public class AttributeMatrixTest {

    private static final String PREP_DIR = "target/legacy_prep/";
    private static final String OUTPUT_DIR = "target/attribute-matrix/";

    /** Value form: constant (absolute value). */
    private static final int KIND_CONSTANT = 0;
    /** Value form: variable. */
    private static final int KIND_VARIABLE = 1;
    /** Value form: expression / formula. */
    private static final int KIND_EXPRESSION = 2;

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

    // ---- Absolute values for diverse types ----

    @DataProvider(name = "constantTypes")
    public Object[][] constantTypes() {
        return new Object[][] {
            {"boolean", "true"},
            {"boolean", "false"},
            {"int", "0"},
            {"int", "-42"},
            {"int", "2147483647"},
            {"long", "123456789012345"},
            {"long", "42"},
            {"float", "1.5"},
            {"double", "2.718"},
            {"String", "\"Hello World\""},
            {"String", "\"\""},
            {"String", "\"Special chars: <>&\\\"' äöü\""},
        };
    }

    /**
     * Absolute values of all basic types must survive the cross-system
     * roundtrip on a host graph node.
     */
    @Test(dataProvider = "constantTypes")
    public void testConstantValueRoundtrip(String typeName, String valueText) throws Exception {
        String baseName = "attr_const_" + sanitize(typeName) + "_" + sanitize(valueText);
        File refFile = prepFile(baseName + ".ggx");

        runDomRoundtripWithVerifier(refFile, baseName, loaded -> {
            Node loadedNode = firstNode(loaded.getGraph());
            if ("long".equals(typeName) && valueText.length() > 9) {
                // Long literals beyond the int range are NOT bound by the
                // Java expression handler (JexHandler/JexExpr parses them as
                // int literals): setExprAsText leaves the member unset
                // (isSet=false), so neither the frozen legacy path nor the
                // DOM path has anything to serialize. The reference contains
                // the AttrType declaration but no value; the DOM load must
                // behave identically: no value survives.
                // (Small long values bind fine and ARE serialized by both
                // paths - see the long/42 case.)
                agg.attribute.AttrInstance attr = loadedNode.getAttribute();
                boolean dropped = attr == null
                    || attr.getNumberOfEntries() == 0
                    || attr.getMemberAt("val") == null
                    || ((ValueMember) attr.getMemberAt("val")).getExpr() == null;
                assertTrue(dropped,
                    "unbound long literals must stay unbound like the frozen "
                        + "legacy path: " + baseName);
            } else {
                verifyValue(loadedNode, "val", typeName, KIND_CONSTANT,
                    stripQuotes(typeName, valueText), baseName);
            }
        });
    }

    // ---- Attribute forms inside the rule morphism mapping ----

    @DataProvider(name = "mappingForms")
    public Object[][] mappingForms() {
        return new Object[][] {
            // {form, lhsText, rhsText, lhsKind, rhsKind, condCount, condText}
            {"constant", "42", "43", KIND_CONSTANT, KIND_CONSTANT, 0, null},
            {"variable", "x", "x + 1", KIND_VARIABLE, KIND_EXPRESSION, 0, null},
            {"expression", "x * 2", "x + 1", KIND_EXPRESSION, KIND_EXPRESSION, 0, null},
            {"condition", "x", "x", KIND_VARIABLE, KIND_VARIABLE, 1, "x > 3"},
        };
    }

    /**
     * Formulas inside the rule morphism: the mapped LHS node carries the
     * input form (constant, variable or expression), the mapped RHS node
     * carries the output form, optionally guarded by an attr condition.
     */
    @Test(dataProvider = "mappingForms")
    public void testRuleAttributeMappingForms(String form, String lhsText, String rhsText,
            int lhsKind, int rhsKind, int condCount, String condText) throws Exception {
        String baseName = "attr_form_" + form;
        File refFile = prepFile(baseName + ".ggx");

        runDomRoundtripWithVerifier(refFile, baseName, loaded -> {
            assertEquals(loaded.getRulesVec().size(), 1, "One rule expected: " + baseName);
            Rule rule = loaded.getRulesVec().get(0);

            // Node mapping preserved
            int mappedNodes = countMappedNodes(rule);
            assertEquals(mappedNodes, 1, "One mapped node expected: " + baseName);

            // LHS and RHS value forms preserved
            Node lhsNode = firstNode(rule.getLeft());
            Node rhsNode = firstNode(rule.getTarget());
            verifyValue(lhsNode, "val", "int", lhsKind, lhsText, baseName + "_lhs");
            verifyValue(rhsNode, "val", "int", rhsKind, rhsText, baseName + "_rhs");

            // Variables and conditions preserved
            verifyVariablesAndConditions(rule, form, condCount, condText, baseName);
        });
    }

    /**
     * Edge attributes inside the rule morphism: the mapped edge carries a
     * variable on the LHS and an expression on the RHS, the mapped nodes
     * carry constants.
     */
    @Test
    public void testEdgeAttributeMappingForm() throws Exception {
        String baseName = "attr_edge_form";
        File refFile = prepFile(baseName + ".ggx");

        runDomRoundtripWithVerifier(refFile, baseName, loaded -> {
            assertEquals(loaded.getRulesVec().size(), 1, "One rule expected: " + baseName);
            Rule loadedRule = loaded.getRulesVec().get(0);

            int mappedNodes = countMappedNodes(loadedRule);
            int mappedArcs = countMappedArcs(loadedRule);
            assertEquals(mappedNodes, 2, "Two mapped nodes expected: " + baseName);
            assertEquals(mappedArcs, 1, "One mapped edge expected: " + baseName);

            Node lhsNode = firstNode(loadedRule.getLeft());
            Node rhsNode = firstNode(loadedRule.getTarget());
            verifyValue(lhsNode, "n", "int", KIND_CONSTANT, "1", baseName + "_lhs_n");
            verifyValue(rhsNode, "n", "int", KIND_CONSTANT, "1", baseName + "_rhs_n");

            agg.xt_basis.Arc lhsArc = firstArc(loadedRule.getLeft());
            agg.xt_basis.Arc rhsArc = firstArc(loadedRule.getTarget());
            verifyValue(lhsArc, "e", "int", KIND_VARIABLE, "x", baseName + "_lhs_e");
            verifyValue(rhsArc, "e", "int", KIND_EXPRESSION, "x + 1", baseName + "_rhs_e");

            // Variable x must exist in the rule attr context
            VarTuple vars = (VarTuple) loadedRule.getAttrContext().getVariables();
            assertNotNull(vars.getVarMemberAt("x"),
                "Variable x should survive the roundtrip: " + baseName);
        });
    }

    // ---- DOM-only long roundtrip stability ----

    /**
     * Long attribute values ARE supported by both serialization paths
     * (the legacy XMLHelper.addAttrValue has a long branch writing
     * &lt;long&gt;...&lt;/long&gt;, and so does the DOM AttributeSerializer).
     * The only limitation is the Java expression handler, which cannot bind
     * long literals beyond the int range (see the cross-system matrix).
     *
     * <p>This test proves the DOM roundtrip with a bindable value: create
     * a long value with the current code, DOM save, DOM load, verify, and
     * check DOM stability (save twice produces identical XML).</p>
     */
    @Test
    public void testLongDomOnlyRoundtrip() throws Exception {
        String baseName = "attr_domonly_long";
        GraGra graGra = TestDataGenerator.createGraphWithTypedNodeValue("long", "42");
        File domFile1 = new File(outputDir, baseName + "_1.ggx");

        // DOM save
        assertTrue(XMLSerialization.saveWithDom(graGra, domFile1.getAbsolutePath()),
            "DOM save of a long value should succeed: " + baseName);
        assertTrue(domFile1.exists() && domFile1.length() > 0,
            "DOM file should be non-empty: " + baseName);

        // DOM load + verify the value survived
        GraGra reloaded = new GraGra();
        XMLSerialization.loadWithDom(reloaded, domFile1.getAbsolutePath());
        Node reloadedNode = firstNode(reloaded.getGraph());
        verifyValue(reloadedNode, "val", "long", KIND_CONSTANT,
            "42", baseName + "_reload");

        // DOM stability: reload + save again must produce identical XML
        File domFile2 = new File(outputDir, baseName + "_2.ggx");
        assertTrue(XMLSerialization.saveWithDom(reloaded, domFile2.getAbsolutePath()),
            "DOM re-save should succeed: " + baseName);
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(domFile1, domFile2);
        assertTrue(result.isEqual(),
            "DOM path should be stable for long values: " + result.getMessage());
    }

    // ---- Helpers ----

    /**
     * Runs the DOM roundtrip against the frozen reference and verifies
     * after the DOM load and after a reload of the DOM save.
     */
    private void runDomRoundtripWithVerifier(File refFile, String baseName,
            java.util.function.Consumer<GraGra> verifier) throws Exception {

        // Step 1: DOM load of the frozen reference + verify
        GraGra graGraFromOld = new GraGra();
        XMLSerialization.loadWithDom(graGraFromOld, refFile.getAbsolutePath());
        assertNotNull(graGraFromOld.getName(),
            "New DOM load of the frozen reference should produce a named GraGra: " + baseName);
        verifier.accept(graGraFromOld);

        // Step 2: DOM save
        File newFile = new File(outputDir, baseName + "_new.ggx");
        assertTrue(XMLSerialization.saveWithDom(graGraFromOld, newFile.getAbsolutePath()),
            "New DOM save should succeed: " + baseName);

        // Step 3: DOM reload + verify again
        GraGra graGraReloaded = new GraGra();
        XMLSerialization.loadWithDom(graGraReloaded, newFile.getAbsolutePath());
        verifier.accept(graGraReloaded);

        // Step 4: canonical comparison against the frozen reference
        XmlCanonicalComparator.ComparisonResult result =
            XmlCanonicalComparator.compareFiles(refFile, newFile);
        assertTrue(result.isEqual(),
            "Cross-system XML mismatch for " + baseName + ": " + result.getMessage());
    }

    /**
     * Verifies the value form and text of an attribute after a load.
     */
    private void verifyValue(GraphObject obj, String attrName, String typeName,
            int expectedKind, String expectedText, String label) {

        ValueMember vm = (ValueMember) obj.getAttribute().getMemberAt(attrName);
        assertNotNull(vm, "Attribute member '" + attrName + "' should exist: " + label);

        HandlerExpr expr = vm.getExpr();
        assertNotNull(expr, "Attribute expression should exist: " + label);

        switch (expectedKind) {
            case KIND_CONSTANT:
                assertTrue(expr.isConstant(),
                    "Expected constant value: " + label + " (got: " + vm.getExprAsText() + ")");
                assertEquals(noSpace(String.valueOf(expr.getValue())),
                    noSpace(expectedText),
                    "Constant value should match: " + label);
                break;
            case KIND_VARIABLE:
                assertTrue(expr.isVariable(),
                    "Expected variable value: " + label + " (got: " + vm.getExprAsText() + ")");
                assertEquals(noSpace(vm.getExprAsText()), noSpace(expectedText),
                    "Variable text should match: " + label);
                break;
            case KIND_EXPRESSION:
                assertFalse(expr.isConstant(),
                    "Expected non-constant expression: " + label);
                assertFalse(expr.isVariable(),
                    "Expected non-variable expression: " + label);
                assertEquals(noSpace(vm.getExprAsText()), noSpace(expectedText),
                    "Expression text should match: " + label);
                break;
            default:
                fail("Unknown kind: " + expectedKind);
        }
    }

    /** Verifies variables and conditions of the rule attr context. */
    private void verifyVariablesAndConditions(Rule rule, String form, int condCount,
            String condText, String label) {

        VarTuple vars = (VarTuple) rule.getAttrContext().getVariables();
        if (!form.equals("constant")) {
            assertNotNull(vars.getVarMemberAt("x"),
                "Variable x should survive the roundtrip: " + label);
        }

        int conditions = rule.getAttrContext().getConditions().getNumberOfEntries();
        assertEquals(conditions, condCount,
            "Condition count should match: " + label);
        if (condText != null) {
            CondMember condMember
                = ((CondTuple) rule.getAttrContext().getConditions()).getCondMemberAt(0);
            assertEquals(noSpace(condMember.getExprAsText()), noSpace(condText),
                "Condition text should match: " + label);
        }
    }

    private int countMappedNodes(Rule rule) {
        int count = 0;
        java.util.Iterator<GraphObject> domain = rule.getDomain();
        while (domain.hasNext()) {
            if (domain.next() instanceof Node) {
                count++;
            }
        }
        return count;
    }

    private int countMappedArcs(Rule rule) {
        int count = 0;
        java.util.Iterator<GraphObject> domain = rule.getDomain();
        while (domain.hasNext()) {
            if (!(domain.next() instanceof Node)) {
                count++;
            }
        }
        return count;
    }

    private Node firstNode(Graph graph) {
        java.util.Iterator<Node> nodes = graph.getNodesSet().iterator();
        if (nodes.hasNext()) {
            return nodes.next();
        }
        return null;
    }

    private agg.xt_basis.Arc firstArc(Graph graph) {
        java.util.Iterator<agg.xt_basis.Arc> arcs = graph.getArcsSet().iterator();
        if (arcs.hasNext()) {
            return arcs.next();
        }
        return null;
    }

    private String stripQuotes(String typeName, String text) {
        if ("String".equals(typeName)) {
            return text.replaceAll("^\"|\"$", "");
        }
        return text;
    }

    private String noSpace(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "");
    }

    private String sanitize(String text) {
        return text.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private File prepFile(String fileName) {
        File refFile = new File(PREP_DIR, fileName);
        assertTrue(refFile.exists() && refFile.length() > 0,
            "Frozen reference file is missing (run the legacy preparation "
                + "suite first: mvn test at the parent, or -pl test/test_agg/legacy_agg): "
                + refFile.getPath());
        return refFile;
    }
}
