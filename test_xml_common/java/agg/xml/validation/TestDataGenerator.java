/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import agg.attribute.facade.impl.DefaultInformationFacade;
import agg.attribute.handler.AttrHandler;
import agg.attribute.impl.DeclTuple;
import agg.attribute.impl.VarTuple;
import agg.parser.ConflictsDependenciesContainer;
import agg.parser.CriticalPairOption;
import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.ruleappl.ObjectFlow;
import agg.ruleappl.RuleSequence;
import agg.util.Pair;
import agg.xt_basis.Arc;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import agg.xt_basis.TypeException;
import agg.xt_basis.agt.RuleScheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Programmatically creates GraGra structures covering all Core domain
 * object types that have their own XML serialization (XwriteObject/XreadObject).
 *
 * <p>Each {@code create*} method returns a GraGra populated with a specific
 * combination of element types. The regression tests use these to verify
 * the old→new→save→compare cycle without depending on external .ggx files.</p>
 */
public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    // ---- Group 1: Basic graph + types + attributes ----

    /**
     * Creates a GraGra with a type graph, node types with attributes,
     * arc types, and a host graph.
     */
    public static GraGra createBasicGraphWithAttributes() throws Exception {
        return createBasicGraphWithAttributes(true);
    }

    /**
     * Creates a GraGra with a basic graph, types and attributes.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createBasicGraphWithAttributes(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("BasicGraphTest");

        // Create node type with attributes
        Type personType = gra.createNodeType(true);
        personType.setStringRepr("Person");
        addAttribute(personType, "name", "String");
        addAttribute(personType, "age", "int");

        // Create arc type with attributes
        Type knowsType = gra.createArcType(true);
        knowsType.setStringRepr("knows");

        // Create type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            Node tgNode1 = typeGraph.createNode(personType);
            Node tgNode2 = typeGraph.createNode(personType);
            Arc tgArc = typeGraph.createArc(knowsType, tgNode1, tgNode2);
        }

        // Use the default host graph (already created by createGraGra(true))
        Graph host = gra.getGraph();
        host.setName("HostGraph");
        Node n1 = host.createNode(personType);
        Node n2 = host.createNode(personType);
        host.createArc(knowsType, n1, n2);

        return gra;
    }

    // ---- Group 2: Rule with NAC, PAC, nested AC ----

    /**
     * Creates a GraGra with a rule containing NAC, PAC, and nested
     * application conditions.
     */
    public static GraGra createRuleWithNacPacNestedAC() throws Exception {
        return createRuleWithNacPacNestedAC(true);
    }

    /**
     * Creates a GraGra with a rule containing NAC, PAC, and nested
     * application conditions.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createRuleWithNacPacNestedAC(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleWithNacPacTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Item");

        Type arcType = gra.createArcType(false);
        arcType.setStringRepr("link");

        // Type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            Node tgN1 = typeGraph.createNode(nodeType);
            Node tgN2 = typeGraph.createNode(nodeType);
            typeGraph.createArc(arcType, tgN1, tgN2);
        }

        // Use the default host graph
        Graph host = gra.getGraph();
        host.setName("Host");
        host.createNode(nodeType);

        // Create rule
        Rule rule = gra.createRule();
        rule.setName("transformItem");
        rule.setLayer(1);
        rule.setPriority(5);

        // Add nodes to LHS and RHS
        Node lhsN1 = rule.getLeft().createNode(nodeType);
        Node lhsN2 = rule.getLeft().createNode(nodeType);
        rule.getLeft().createArc(arcType, lhsN1, lhsN2);

        Node rhsN1 = rule.getTarget().createNode(nodeType);
        // Map lhsN1 → rhsN1
        rule.addMapping(lhsN1, rhsN1);

        // Create NAC
        OrdinaryMorphism nac = rule.createNAC();
        Node nacN = nac.getTarget().createNode(nodeType);
        nac.addMapping(lhsN1, nacN);
        rule.addNAC(nac);

        // Create PAC
        OrdinaryMorphism pac = rule.createPAC();
        Node pacN = pac.getTarget().createNode(nodeType);
        pac.addMapping(lhsN1, pacN);
        rule.addPAC(pac);

        // Create nested AC
        OrdinaryMorphism nestedAC = rule.createNestedAC();
        Node acN = nestedAC.getTarget().createNode(nodeType);
        nestedAC.addMapping(lhsN1, acN);
        rule.addNestedAC(nestedAC);

        gra.addRule(rule);

        return gra;
    }

    // ---- Group 3: Constraints (Formula + AtomConstraint) ----

    /**
     * Creates a GraGra with atomic constraints and a formula.
     */
    public static GraGra createWithConstraints() throws Exception {
        return createWithConstraints(true);
    }

    /**
     * Creates a GraGra with constraints.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createWithConstraints(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("ConstraintTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        Type arcType = gra.createArcType(false);
        arcType.setStringRepr("edge");

        // Type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            Node tgN1 = typeGraph.createNode(nodeType);
            Node tgN2 = typeGraph.createNode(nodeType);
            typeGraph.createArc(arcType, tgN1, tgN2);
        }

        // Use the default host graph
        Graph host = gra.getGraph();
        host.setName("Host");
        host.createNode(nodeType);

        // Create atomic constraint
        gra.createAtomic("AtomicConstraint1");

        // Create formula
        gra.createConstraint("Formula1");

        return gra;
    }

    // ---- Group 4: Match ----

    /**
     * Creates a GraGra with a rule and a match.
     */
    public static GraGra createWithMatch() throws Exception {
        return createWithMatch(true);
    }

    /**
     * Creates a GraGra with a rule and a match.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createWithMatch(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("MatchTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            typeGraph.createNode(nodeType);
        }

        // Use the default host graph
        Graph host = gra.getGraph();
        host.setName("Host");
        Node hN1 = host.createNode(nodeType);
        Node hN2 = host.createNode(nodeType);

        // Rule: delete a node
        Rule rule = gra.createRule();
        rule.setName("deleteNode");
        Node lhsN = rule.getLeft().createNode(nodeType);
        // RHS is empty (delete)
        gra.addRule(rule);

        // Create match with a mapping (non-empty match survives roundtrip)
        agg.xt_basis.Match match = gra.createMatch(rule);
        match.addMapping(lhsN, hN1);

        return gra;
    }

    // ---- Group 5: RuleScheme (MultiRule / KernelRule) ----

    /**
     * Creates a GraGra with a RuleScheme containing a kernel rule and
     * a multi-rule.
     */
    public static GraGra createWithRuleScheme() throws Exception {
        return createWithRuleScheme(true);
    }

    /**
     * Creates a GraGra with a RuleScheme containing a kernel rule and
     * a multi-rule.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createWithRuleScheme(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleSchemeTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            typeGraph.createNode(nodeType);
        }

        // Use the default host graph
        Graph host = gra.getGraph();
        host.setName("Host");
        host.createNode(nodeType);

        // Create rule scheme
        RuleScheme rs = gra.createRuleScheme();
        // Kernel rule already has empty LHS/RHS; add a node to LHS
        Rule kernelRule = rs.getKernelRule();
        kernelRule.setName("kernelRule");
        kernelRule.getLeft().createNode(nodeType);
        kernelRule.getTarget().createNode(nodeType);

        // Add a multi-rule
        Rule multiRule = rs.addMultiRule("multiRule1");
        multiRule.getLeft().createNode(nodeType);
        multiRule.getTarget().createNode(nodeType);

        gra.addRuleScheme(rs);

        return gra;
    }

    // ---- Group 6: RuleSequence + ApplRuleSequence ----

    /**
     * Creates a GraGra with rules and a RuleSequence.
     */
    public static GraGra createWithRuleSequence() throws Exception {
        return createWithRuleSequence(true);
    }

    /**
     * Creates a GraGra with rules and a RuleSequence.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createWithRuleSequence(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleSequenceTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            typeGraph.createNode(nodeType);
        }

        // Use the default host graph
        Graph host = gra.getGraph();
        host.setName("Host");
        host.createNode(nodeType);

        // Create two rules
        Rule rule1 = gra.createRule();
        rule1.setName("rule1");
        rule1.getLeft().createNode(nodeType);
        rule1.getTarget().createNode(nodeType);
        gra.addRule(rule1);

        Rule rule2 = gra.createRule();
        rule2.setName("rule2");
        rule2.getLeft().createNode(nodeType);
        rule2.getTarget().createNode(nodeType);
        gra.addRule(rule2);

        // Create rule sequence
        RuleSequence seq = gra.createRuleSequence("TestSequence");
        seq.addRule(rule1);
        seq.addRule(rule2);
        seq.setTrafoByARS(true);

        return gra;
    }

    /**
     * Creates an ApplRuleSequence from a GraGra with rule sequences.
     */
    public static ApplRuleSequence createApplRuleSequence(GraGra gra) {
        CriticalPairOption option = new CriticalPairOption();
        ApplRuleSequence ars = new ApplRuleSequence(option);
        ars.setGraGra(gra);
        return ars;
    }

    // ---- Morphism matrix scenario (shared between legacy preparation and DOM tests) ----

    /** Morphism matrix structure: empty graph side. */
    public static final int STRUCT_NOTHING = 0;
    /** Morphism matrix structure: two nodes only. */
    public static final int STRUCT_NODES = 1;
    /** Morphism matrix structure: two nodes plus one edge. */
    public static final int STRUCT_NODES_EDGES = 2;

    /** Morphism matrix mapping: nothing mapped. */
    public static final int MAP_NOTHING = 0;
    /** Morphism matrix mapping: both nodes mapped. */
    public static final int MAP_NODES = 1;
    /** Morphism matrix mapping: both nodes plus the edge mapped. */
    public static final int MAP_NODES_EDGES = 2;

    /**
     * Creates a GraGra with a single rule whose LHS, RHS and morphism are
     * configured according to the morphism matrix cell.
     *
     * @param lhsSpec LHS structure (STRUCT_*)
     * @param rhsSpec RHS structure (STRUCT_*)
     * @param mapSpec mapping (MAP_*)
     * @param directed arc orientation of the type set
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createMorphismGraGra(int lhsSpec, int rhsSpec, int mapSpec,
            boolean directed, boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("MorphismMatrixTest");
        gra.getTypeSet().setArcDirected(directed);

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");
        Type edgeType = gra.createArcType(false);
        edgeType.setStringRepr("Edge");

        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            typeGraph.createNode(nodeType);
        }

        Graph host = gra.getGraph();
        host.createNode(nodeType);

        Rule rule = gra.createRule();
        rule.setName("matrixRule");

        Node lhsN1 = null;
        Node lhsN2 = null;
        Arc lhsArc = null;
        if (lhsSpec >= STRUCT_NODES) {
            lhsN1 = rule.getLeft().createNode(nodeType);
            lhsN2 = rule.getLeft().createNode(nodeType);
        }
        if (lhsSpec == STRUCT_NODES_EDGES) {
            lhsArc = rule.getLeft().createArc(edgeType, lhsN1, lhsN2);
        }

        Node rhsN1 = null;
        Node rhsN2 = null;
        Arc rhsArc = null;
        if (rhsSpec >= STRUCT_NODES) {
            rhsN1 = rule.getTarget().createNode(nodeType);
            rhsN2 = rule.getTarget().createNode(nodeType);
        }
        if (rhsSpec == STRUCT_NODES_EDGES) {
            rhsArc = rule.getTarget().createArc(edgeType, rhsN1, rhsN2);
        }

        if (mapSpec >= MAP_NODES) {
            rule.addMapping(lhsN1, rhsN1);
            rule.addMapping(lhsN2, rhsN2);
        }
        if (mapSpec == MAP_NODES_EDGES) {
            rule.addMapping(lhsArc, rhsArc);
        }

        gra.addRule(rule);
        return gra;
    }

    // ---- Attribute matrix scenarios (shared between legacy preparation and DOM tests) ----

    /**
     * Creates a GraGra whose host graph holds a single node with one
     * attribute of the given type set to the given constant value text.
     */
    public static GraGra createGraphWithTypedNodeValue(String typeName, String valueText)
            throws Exception {
        GraGra graGra = BaseFactory.theFactory().createGraGra(true);
        graGra.setName("AttributeMatrixTest");
        Type nodeType = graGra.createNodeType(false);
        nodeType.setStringRepr("T_" + typeName);
        addAttribute(nodeType, "val", typeName);

        Graph host = graGra.getGraph();
        Node hostNode = host.createNode(nodeType);
        setValue(hostNode, "val", valueText);
        return graGra;
    }

    /**
     * Creates a GraGra with one rule; the single mapped node pair carries
     * the given attribute value forms, optionally guarded by an attr condition.
     */
    public static GraGra createRuleWithAttributes(String attrName, String typeName,
            String lhsText, String rhsText, String condText) throws Exception {

        GraGra graGra = BaseFactory.theFactory().createGraGra(true);
        graGra.setName("AttributeMatrixTest");
        graGra.getTypeSet().setArcDirected(true);

        AttrHandler handler = DefaultInformationFacade.self().getJavaHandler();
        Type nodeType = graGra.createNodeType(false);
        nodeType.setStringRepr("N");
        addAttribute(nodeType, attrName, typeName);

        Graph host = graGra.getGraph();
        Node h = host.createNode(nodeType);
        setValue(h, attrName, "7");

        Rule rule = graGra.createRule();
        rule.setName("attrRule");
        ((VarTuple) rule.getAttrContext().getVariables()).declare(handler, "int", "x");

        Node lhsNode = rule.getLeft().createNode(nodeType);
        setValue(lhsNode, attrName, lhsText);
        Node rhsNode = rule.getTarget().createNode(nodeType);
        setValue(rhsNode, attrName, rhsText);
        rule.addMapping(lhsNode, rhsNode);

        if (condText != null) {
            rule.getAttrContext().getConditions().addCondition(condText);
        }

        graGra.addRule(rule);
        return graGra;
    }

    /**
     * Creates a GraGra with a rule whose mapped edge carries a variable
     * on the LHS and an expression on the RHS; the mapped nodes carry
     * constants. Node and edge types each have an int attribute.
     */
    public static GraGra createEdgeAttributeRule() throws Exception {
        GraGra graGra = BaseFactory.theFactory().createGraGra(true);
        graGra.setName("AttributeMatrixTest");
        graGra.getTypeSet().setArcDirected(true);

        AttrHandler handler = DefaultInformationFacade.self().getJavaHandler();
        Type nodeType = graGra.createNodeType(false);
        nodeType.setStringRepr("N");
        addAttribute(nodeType, "n", "int");
        Type edgeType = graGra.createArcType(false);
        edgeType.setStringRepr("E");
        addAttribute(edgeType, "e", "int");

        Graph host = graGra.getGraph();
        Node h1 = host.createNode(nodeType);
        Node h2 = host.createNode(nodeType);
        host.createArc(edgeType, h1, h2);
        setValue(h1, "n", "1");
        setValue(h2, "n", "2");

        Rule rule = graGra.createRule();
        rule.setName("edgeRule");
        ((VarTuple) rule.getAttrContext().getVariables()).declare(handler, "int", "x");

        Node l1 = rule.getLeft().createNode(nodeType);
        Node l2 = rule.getLeft().createNode(nodeType);
        Arc lArc = rule.getLeft().createArc(edgeType, l1, l2);
        setValue(l1, "n", "1");
        setValue(l2, "n", "2");
        setValue(lArc, "e", "x");

        Node r1 = rule.getTarget().createNode(nodeType);
        Node r2 = rule.getTarget().createNode(nodeType);
        Arc rArc = rule.getTarget().createArc(edgeType, r1, r2);
        setValue(r1, "n", "1");
        setValue(r2, "n", "2");
        setValue(rArc, "e", "x + 1");

        rule.addMapping(l1, r1);
        rule.addMapping(l2, r2);
        rule.addMapping(lArc, rArc);
        graGra.addRule(rule);
        return graGra;
    }

    /**
     * Creates a GraGra combining as many element types as possible:
     * rule with NAC/PAC/nested AC, atomic constraint, formula, a second
     * rule with a match mapping and a rule sequence.
     */
    public static GraGra createCompositeAll() throws Exception {
        return createCompositeAll(true);
    }

    /**
     * Creates a GraGra combining as many element types as possible:
     * rule with NAC/PAC/nested AC, atomic constraint, formula, a second
     * rule with a match mapping and a rule sequence.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createCompositeAll(boolean withTypeGraph) throws Exception {
        GraGra graGra = createRuleWithNacPacNestedAC(withTypeGraph);

        graGra.createAtomic("compositeAtomic");
        graGra.createConstraint("compositeFormula");

        Rule rule2 = graGra.createRule();
        rule2.setName("compositeRule2");
        rule2.setLayer(2);
        rule2.setPriority(3);
        de.jare.ndimcol.ref.IteratorWalker<Type> typeIter =
            graGra.getTypeSet().getTypeWalker();
        Type firstType = typeIter != null && typeIter.hasNext() ? typeIter.next() : null;
        if (firstType != null) {
            Node compositeLhsN = rule2.getLeft().createNode(firstType);
            rule2.getTarget().createNode(firstType);
            graGra.addRule(rule2);

            agg.xt_basis.Match compositeMatch = graGra.createMatch(rule2);
            if (graGra.getGraph() != null && graGra.getGraph().getNodesCount() > 0) {
                compositeMatch.addMapping(compositeLhsN,
                    graGra.getGraph().getNodesSet().iterator().next());
            }
        } else {
            graGra.addRule(rule2);
        }

        graGra.createRuleSequence("compositeSequence");
        return graGra;
    }

    /** Sets the attribute value expression text on a graph object. */
    public static void setValue(agg.xt_basis.GraphObject obj, String attrName, String text) {
        agg.attribute.impl.ValueMember vm
            = (agg.attribute.impl.ValueMember) obj.getAttribute().getMemberAt(attrName);
        vm.setExprAsText(text);
    }

    // ---- Group 7: ConflictsDependenciesContainer ----

    /**
     * Creates a ConflictsDependenciesContainer from a GraGra.
     */
    public static ConflictsDependenciesContainer createConflictsDependenciesContainer(GraGra gra) {
        ExcludePairContainer excludePC = new ExcludePairContainer(gra);
        DependencyPairContainer depPC = new DependencyPairContainer(gra);
        return new ConflictsDependenciesContainer(excludePC, depPC);
    }

    // ---- Helpers ----

    /**
     * Adds an attribute declaration to a type.
     */
    private static void addAttribute(Type type, String name, String typeName) {
        if (type.getAttrType() == null) {
            type.createAttributeType();
        }
        DeclTuple declTuple = (DeclTuple) type.getAttrType();
        AttrHandler handler = DefaultInformationFacade.self().getJavaHandler();
        declTuple.addMember(handler, typeName, name);
    }
}
