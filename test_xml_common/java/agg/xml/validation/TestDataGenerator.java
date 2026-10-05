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
import agg.xt_basis.GraphObject;
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

    /**
     * Creates a GraGra with a RuleScheme whose kernel rule has a NAC, to
     * cover application conditions inside a scheme section.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createWithRuleSchemeNac(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleSchemeNacTest");

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

        // Create rule scheme with a kernel NAC
        RuleScheme rs = gra.createRuleScheme();
        Rule kernelRule = rs.getKernelRule();
        kernelRule.setName("kernelRule");
        Node kernL = kernelRule.getLeft().createNode(nodeType);
        kernelRule.getTarget().createNode(nodeType);

        OrdinaryMorphism nac = kernelRule.createNAC();
        Node nacN = nac.getTarget().createNode(nodeType);
        nac.addMapping(kernL, nacN);
        kernelRule.addNAC(nac);

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

    // ---- Group 7b: computed critical pairs ----

    /**
     * Creates a GraGra whose rules yield delete-use conflicts and
     * produce-deliver dependencies when the CPA engine runs.
     */
    public static GraGra createCpaGraGra() throws Exception {
        return createCpaGraGra(false);
    }

    /**
     * Creates a GraGra whose rules yield delete-use conflicts and
     * produce-deliver dependencies when the CPA engine runs.
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createCpaGraGra(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("CpaComputedTest");

        Type itemType = gra.createNodeType(false);
        itemType.setStringRepr("Item");
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            typeGraph.createNode(itemType);
        }

        // useItem: Item -> Item (identity mapping)
        Rule useItem = gra.createRule();
        useItem.setName("useItem");
        Node useLhs = useItem.getLeft().createNode(itemType);
        Node useRhs = useItem.getTarget().createNode(itemType);
        useItem.addMapping(useLhs, useRhs);

        // deleteItem: Item -> (empty)
        Rule deleteItem = gra.createRule();
        deleteItem.setName("deleteItem");
        deleteItem.getLeft().createNode(itemType);

        // createItem: Item -> Item, Item (produces a second Item)
        Rule createItem = gra.createRule();
        createItem.setName("createItem");
        Node createLhs = createItem.getLeft().createNode(itemType);
        Node createRhs1 = createItem.getTarget().createNode(itemType);
        createItem.getTarget().createNode(itemType);
        createItem.addMapping(createLhs, createRhs1);

        gra.addRule(useItem);
        gra.addRule(deleteItem);
        gra.addRule(createItem);
        return gra;
    }

    /**
     * Creates a GraGra whose rules yield produce-forbid conflicts backed by
     * a NAC overlap (exercises the NAC+LHS morphism branches of the .cpx
     * format).
     */
    public static GraGra createCpaNacGraGra() throws Exception {
        return createCpaNacGraGra(false);
    }

    /**
     * Creates a GraGra whose rules yield produce-forbid conflicts backed by
     * a NAC overlap (exercises the NAC+LHS morphism branches of the .cpx
     * format).
     *
     * @param withTypeGraph whether to create a type graph
     */
    public static GraGra createCpaNacGraGra(boolean withTypeGraph) throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("CpaNacComputedTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Item");
        Type linkType = gra.createArcType(false);
        linkType.setStringRepr("link");
        if (withTypeGraph) {
            Graph typeGraph = gra.createTypeGraph();
            Node tgN1 = typeGraph.createNode(nodeType);
            Node tgN2 = typeGraph.createNode(nodeType);
            typeGraph.createArc(linkType, tgN1, tgN2);
        }

        // makeLink: {a, b} -> {a, b, a-link->b}
        Rule makeLink = gra.createRule();
        makeLink.setName("makeLink");
        Node lhsA = makeLink.getLeft().createNode(nodeType);
        Node lhsB = makeLink.getLeft().createNode(nodeType);
        Node rhsA = makeLink.getTarget().createNode(nodeType);
        Node rhsB = makeLink.getTarget().createNode(nodeType);
        makeLink.addMapping(lhsA, rhsA);
        makeLink.addMapping(lhsB, rhsB);
        makeLink.getTarget().createArc(linkType, rhsA, rhsB);

        // forbidLink: {a, b} -> {a, b} with NAC {a, b, a-link->b}
        Rule forbidLink = gra.createRule();
        forbidLink.setName("forbidLink");
        Node fLhsA = forbidLink.getLeft().createNode(nodeType);
        Node fLhsB = forbidLink.getLeft().createNode(nodeType);
        Node fRhsA = forbidLink.getTarget().createNode(nodeType);
        Node fRhsB = forbidLink.getTarget().createNode(nodeType);
        forbidLink.addMapping(fLhsA, fRhsA);
        forbidLink.addMapping(fLhsB, fRhsB);
        OrdinaryMorphism nac = forbidLink.createNAC();
        nac.setName("forbidLinkNac");
        Node nacA = nac.getTarget().createNode(nodeType);
        Node nacB = nac.getTarget().createNode(nodeType);
        nac.addMapping(fLhsA, nacA);
        nac.addMapping(fLhsB, nacB);
        nac.getTarget().createArc(linkType, nacA, nacB);
        forbidLink.addNAC(nac);

        gra.addRule(makeLink);
        gra.addRule(forbidLink);
        return gra;
    }

    /**
     * Creates a GraGra whose rules yield a produce-forbid conflict backed by
     * a PAC overlap (exercises the PAC+LHS morphism branches of the .cpx
     * format).
     */
    public static GraGra createCpaPacGraGra() throws Exception {
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("CpaPacComputedTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Item");
        Type linkType = gra.createArcType(false);
        linkType.setStringRepr("link");

        // makeLinkPac: {a, b} -> {a, b, a-link->b}
        Rule makeLinkPac = gra.createRule();
        makeLinkPac.setName("makeLinkPac");
        Node lhsA = makeLinkPac.getLeft().createNode(nodeType);
        Node lhsB = makeLinkPac.getLeft().createNode(nodeType);
        Node rhsA = makeLinkPac.getTarget().createNode(nodeType);
        Node rhsB = makeLinkPac.getTarget().createNode(nodeType);
        makeLinkPac.addMapping(lhsA, rhsA);
        makeLinkPac.addMapping(lhsB, rhsB);
        makeLinkPac.getTarget().createArc(linkType, rhsA, rhsB);

        // needLinkPac: {a, b} -> {a, b} with PAC {a, b, a-link->b}
        Rule needLinkPac = gra.createRule();
        needLinkPac.setName("needLinkPac");
        Node nLhsA = needLinkPac.getLeft().createNode(nodeType);
        Node nLhsB = needLinkPac.getLeft().createNode(nodeType);
        Node nRhsA = needLinkPac.getTarget().createNode(nodeType);
        Node nRhsB = needLinkPac.getTarget().createNode(nodeType);
        needLinkPac.addMapping(nLhsA, nRhsA);
        needLinkPac.addMapping(nLhsB, nRhsB);
        OrdinaryMorphism pac = needLinkPac.createPAC();
        pac.setName("needLinkPacPac");
        Node pacA = pac.getTarget().createNode(nodeType);
        Node pacB = pac.getTarget().createNode(nodeType);
        pac.addMapping(nLhsA, pacA);
        pac.addMapping(nLhsB, pacB);
        pac.getTarget().createArc(linkType, pacA, pacB);
        needLinkPac.addPAC(pac);

        gra.addRule(makeLinkPac);
        gra.addRule(needLinkPac);
        return gra;
    }

    /**
     * Creates a ConflictsDependenciesContainer with synthetic computed
     * conflict and dependency entries (Overlapping_Pair content).
     *
     * <p>The CPA engine of this code base cannot produce pair entries (its
     * inclusion helper never adds the generated inclusions to the result),
     * so the entries are constructed synthetically. The structures mirror
     * exactly what the .cpx reader reconstructs, so the legacy writer and
     * the DOM writer serialize them identically.</p>
     */
    public static ConflictsDependenciesContainer createComputedConflictsDependenciesContainer(GraGra gra) throws Exception {
        ExcludePairContainer excludePC = new ExcludePairContainer(gra);
        DependencyPairContainer depPC = new DependencyPairContainer(gra);
        java.util.List<Rule> rules = gra.getListOfRules();
        excludePC.setRules(rules, rules);
        depPC.setRules(rules, rules);
        if ("CpaNacComputedTest".equals(gra.getName())) {
            addNacConflictEntry(excludePC, gra);
        } else if ("CpaPacComputedTest".equals(gra.getName())) {
            addPacConflictEntry(excludePC, gra);
        } else {
            addPlainConflictEntries(excludePC, gra);
            addPlainDependencyEntries(depPC, gra);
        }
        return new ConflictsDependenciesContainer(excludePC, depPC);
    }

    private static Rule findRule(GraGra gra, String name) {
        for (Rule rule : gra.getListOfRules()) {
            if (name.equals(rule.getName())) {
                return rule;
            }
        }
        return null;
    }

    private static void addPlainConflictEntries(ExcludePairContainer pc, GraGra gra) throws Exception {
        Rule deleteItem = findRule(gra, "deleteItem");
        Rule useItem = findRule(gra, "useItem");
        Rule createItem = findRule(gra, "createItem");

        // deleteItem x useItem: delete-use overlap with one critical node
        Graph duOverlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        duOverlap.setName("delete-use-conflict");
        Node duNode = duOverlap.createNode(
            deleteItem.getLeft().getNodesSet().iterator().next().getType());
        duNode.setCritical(true);
        OrdinaryMorphism duFirst = BaseFactory.theFactory().createMorphism(
            deleteItem.getLeft(), duOverlap);
        duFirst.addMapping(deleteItem.getLeft().getNodesSet().iterator().next(), duNode);
        duFirst.setName("first");
        OrdinaryMorphism duSecond = BaseFactory.theFactory().createMorphism(
            useItem.getLeft(), duOverlap);
        duSecond.addMapping(useItem.getLeft().getNodesSet().iterator().next(), duNode);
        duSecond.setName("second");
        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> duOverlaps
            = new ArrayList<>();
        duOverlaps.add(new Pair<>(new Pair<>(duFirst, duSecond), null));
        pc.addQuadruple(pc.getExcludeContainer(), deleteItem, useItem, true, duOverlaps);
        pc.getEntry(deleteItem, useItem);
        pc.addQuadruple(pc.getConflictFreeContainer(), useItem, deleteItem, true, null);
        pc.getEntry(useItem, deleteItem);

        // createItem x useItem: produce-forbid overlap (plain, no NAC pair)
        Graph pfOverlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        pfOverlap.setName("produce-forbid-conflict");
        Node pfNode = pfOverlap.createNode(
            useItem.getLeft().getNodesSet().iterator().next().getType());
        OrdinaryMorphism pfFirst = BaseFactory.theFactory().createMorphism(
            createItem.getRight(), pfOverlap);
        for (Node rhsNode : createItem.getRight().getNodesSet()) {
            pfFirst.addMapping(rhsNode, pfNode);
        }
        pfFirst.setName("first");
        OrdinaryMorphism pfSecond = BaseFactory.theFactory().createMorphism(
            useItem.getLeft(), pfOverlap);
        pfSecond.addMapping(useItem.getLeft().getNodesSet().iterator().next(), pfNode);
        pfSecond.setName("second");
        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> pfOverlaps
            = new ArrayList<>();
        pfOverlaps.add(new Pair<>(new Pair<>(pfFirst, pfSecond), null));
        pc.addQuadruple(pc.getExcludeContainer(), createItem, useItem, true, pfOverlaps);
        pc.getEntry(createItem, useItem);
    }

    private static void addPlainDependencyEntries(DependencyPairContainer pc, GraGra gra) throws Exception {
        Rule deleteItem = findRule(gra, "deleteItem");
        Rule useItem = findRule(gra, "useItem");
        Rule createItem = findRule(gra, "createItem");

        // createItem x deleteItem: deliver-delete overlap
        Graph ddOverlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        ddOverlap.setName("deliver-delete-dependency");
        Node ddNode = ddOverlap.createNode(
            deleteItem.getLeft().getNodesSet().iterator().next().getType());
        OrdinaryMorphism ddFirst = BaseFactory.theFactory().createMorphism(
            createItem.getRight(), ddOverlap);
        for (Node rhsNode : createItem.getRight().getNodesSet()) {
            ddFirst.addMapping(rhsNode, ddNode);
        }
        ddFirst.setName("first");
        OrdinaryMorphism ddSecond = BaseFactory.theFactory().createMorphism(
            deleteItem.getLeft(), ddOverlap);
        ddSecond.addMapping(deleteItem.getLeft().getNodesSet().iterator().next(), ddNode);
        ddSecond.setName("second");
        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> ddOverlaps
            = new ArrayList<>();
        ddOverlaps.add(new Pair<>(new Pair<>(ddFirst, ddSecond), null));
        pc.addQuadruple(pc.getExcludeContainer(), createItem, deleteItem, true, ddOverlaps);
        pc.getEntry(createItem, deleteItem);

        // createItem x useItem: deliver-delete overlap (second pair)
        Graph duOverlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        duOverlap.setName("deliver-delete-dependency");
        Node duNode = duOverlap.createNode(
            useItem.getLeft().getNodesSet().iterator().next().getType());
        OrdinaryMorphism duFirst = BaseFactory.theFactory().createMorphism(
            createItem.getRight(), duOverlap);
        for (Node rhsNode : createItem.getRight().getNodesSet()) {
            duFirst.addMapping(rhsNode, duNode);
        }
        duFirst.setName("first");
        OrdinaryMorphism duSecond = BaseFactory.theFactory().createMorphism(
            useItem.getLeft(), duOverlap);
        duSecond.addMapping(useItem.getLeft().getNodesSet().iterator().next(), duNode);
        duSecond.setName("second");
        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> duOverlaps
            = new ArrayList<>();
        duOverlaps.add(new Pair<>(new Pair<>(duFirst, duSecond), null));
        pc.addQuadruple(pc.getExcludeContainer(), createItem, useItem, true, duOverlaps);
        pc.getEntry(createItem, useItem);

        // useItem x deleteItem: non-critical pair (bool=false)
        pc.addQuadruple(pc.getExcludeContainer(), useItem, deleteItem, false, null);
        pc.getEntry(useItem, deleteItem);

        // dependency-free entry marked as not computable
        pc.addQuadruple(pc.getConflictFreeContainer(), deleteItem, createItem, true, null);
        ExcludePairContainer.Entry ddEntry = pc.getEntry(deleteItem, createItem);
        ddEntry.setStatus(ExcludePairContainer.Entry.NOT_COMPUTABLE);
    }

    private static void addNacConflictEntry(ExcludePairContainer pc, GraGra gra) throws Exception {
        Rule makeLink = findRule(gra, "makeLink");
        Rule forbidLink = findRule(gra, "forbidLink");
        OrdinaryMorphism nac = forbidLink.getNACsList().get(0);

        // extend the LHS of r2 by the NAC (mirrors the .cpx reader)
        Pair<OrdinaryMorphism, OrdinaryMorphism> nacLhs =
            BaseFactory.theFactory().extendLeftGraphByNAC(forbidLink, nac);
        OrdinaryMorphism morphL2iso = nacLhs.first;
        OrdinaryMorphism morphNACiso = nacLhs.second;
        Graph extLeft = morphL2iso.getTarget();

        // the extended LHS contains exactly one arc (from the NAC)
        Arc extArc = extLeft.getArcsSet().iterator().next();
        Node extSrc = (Node) extArc.getSource();
        Node extTar = (Node) extArc.getTarget();

        // overlap graph: one critical arc plus its endpoints
        Graph overlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        overlap.setName("produce-forbid-conflict");
        overlap.setHelpInfo("NAC:" + nac.getName());
        Node ovSrc = overlap.createNode(extSrc.getType());
        Node ovTar = overlap.createNode(extTar.getType());
        Arc ovArc = overlap.createArc(extArc.getType(), ovSrc, ovTar);
        ovArc.setCritical(true);
        ovSrc.setCritical(true);

        // second morphism: extended LHS -> overlap
        OrdinaryMorphism second = BaseFactory.theFactory().createMorphism(extLeft, overlap);
        second.addMapping(extSrc, ovSrc);
        second.addMapping(extTar, ovTar);
        second.addMapping(extArc, ovArc);
        second.setName("second");

        // first morphism: RHS of r1 -> overlap
        Arc rhsArc = makeLink.getRight().getArcsSet().iterator().next();
        OrdinaryMorphism first = BaseFactory.theFactory().createMorphism(
            makeLink.getRight(), overlap);
        first.addMapping((Node) rhsArc.getSource(), ovSrc);
        first.addMapping((Node) rhsArc.getTarget(), ovTar);
        first.addMapping(rhsArc, ovArc);
        first.setName("first");

        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> overlaps
            = new ArrayList<>();
        overlaps.add(new Pair<>(new Pair<>(first, second),
            new Pair<>(morphL2iso, morphNACiso)));
        pc.addQuadruple(pc.getExcludeContainer(), makeLink, forbidLink, true, overlaps);
        pc.getEntry(makeLink, forbidLink);
    }

    private static void addPacConflictEntry(ExcludePairContainer pc, GraGra gra) throws Exception {
        Rule makeLinkPac = findRule(gra, "makeLinkPac");
        Rule needLinkPac = findRule(gra, "needLinkPac");
        OrdinaryMorphism pac = needLinkPac.getPACsList().get(0);

        // extend the LHS of r2 by the PAC (mirrors the .cpx reader)
        OrdinaryMorphism morphL2iso = needLinkPac.getLeft().isoCopy();
        Graph extLeft = morphL2iso.getTarget();
        Arc pacArc = pac.getTarget().getArcsSet().iterator().next();
        Node pacSrc = (Node) pacArc.getSource();
        Node pacTar = (Node) pacArc.getTarget();
        Node extSrc = null;
        Node extTar = null;
        for (java.util.Iterator<GraphObject> dom = pac.getDomain(); dom.hasNext();) {
            GraphObject lhsObj = dom.next();
            GraphObject pacImg = pac.getImage(lhsObj);
            if (pacImg == pacSrc) {
                extSrc = (Node) morphL2iso.getImage(lhsObj);
            } else if (pacImg == pacTar) {
                extTar = (Node) morphL2iso.getImage(lhsObj);
            }
        }
        Arc extArc = extLeft.createArc(pacArc.getType(), extSrc, extTar);

        // embedPac: PAC graph -> extended LHS
        OrdinaryMorphism embedPac = BaseFactory.theFactory().createMorphism(
            pac.getTarget(), extLeft);
        embedPac.addMapping(pacSrc, extSrc);
        embedPac.addMapping(pacTar, extTar);
        embedPac.addMapping(pacArc, extArc);

        // overlap graph: one critical arc plus its endpoints
        Graph overlap = BaseFactory.theFactory().createGraph(gra.getTypeSet());
        overlap.setName("delete-need(PAC:" + pac.getName() + ")");
        overlap.setHelpInfo("PAC:" + pac.getName());
        Node ovSrc = overlap.createNode(extSrc.getType());
        Node ovTar = overlap.createNode(extTar.getType());
        Arc ovArc = overlap.createArc(pacArc.getType(), ovSrc, ovTar);
        ovArc.setCritical(true);

        // morphL2PACiso: extended LHS -> overlap
        OrdinaryMorphism morphL2PACiso = BaseFactory.theFactory().createMorphism(extLeft, overlap);
        morphL2PACiso.addMapping(extSrc, ovSrc);
        morphL2PACiso.addMapping(extTar, ovTar);
        morphL2PACiso.addMapping(extArc, ovArc);

        // second morphism: LHS of r2 -> overlap
        OrdinaryMorphism second = BaseFactory.theFactory().createMorphism(
            needLinkPac.getLeft(), overlap);
        for (java.util.Iterator<GraphObject> dom = pac.getDomain(); dom.hasNext();) {
            GraphObject lhsObj = dom.next();
            second.addMapping(lhsObj, morphL2PACiso.getImage(morphL2iso.getImage(lhsObj)));
        }
        second.setName("second");

        // first morphism: RHS of r1 -> overlap
        Arc rhsArc = makeLinkPac.getRight().getArcsSet().iterator().next();
        OrdinaryMorphism first = BaseFactory.theFactory().createMorphism(
            makeLinkPac.getRight(), overlap);
        first.addMapping((Node) rhsArc.getSource(), ovSrc);
        first.addMapping((Node) rhsArc.getTarget(), ovTar);
        first.addMapping(rhsArc, ovArc);
        first.setName("first");

        java.util.List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> overlaps
            = new ArrayList<>();
        overlaps.add(new Pair<>(new Pair<>(first, second),
            new Pair<>(embedPac, morphL2PACiso)));
        pc.addQuadruple(pc.getExcludeContainer(), makeLinkPac, needLinkPac, true, overlaps);
        pc.getEntry(makeLinkPac, needLinkPac);
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
