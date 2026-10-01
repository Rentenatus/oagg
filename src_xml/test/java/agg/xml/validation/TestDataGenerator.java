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
        Graph typeGraph = gra.createTypeGraph();
        Node tgNode1 = typeGraph.createNode(personType);
        Node tgNode2 = typeGraph.createNode(personType);
        Arc tgArc = typeGraph.createArc(knowsType, tgNode1, tgNode2);

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
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleWithNacPacTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Item");

        Type arcType = gra.createArcType(false);
        arcType.setStringRepr("link");

        // Type graph
        Graph typeGraph = gra.createTypeGraph();
        Node tgN1 = typeGraph.createNode(nodeType);
        Node tgN2 = typeGraph.createNode(nodeType);
        typeGraph.createArc(arcType, tgN1, tgN2);

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
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("ConstraintTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        Type arcType = gra.createArcType(false);
        arcType.setStringRepr("edge");

        // Type graph
        Graph typeGraph = gra.createTypeGraph();
        Node tgN1 = typeGraph.createNode(nodeType);
        Node tgN2 = typeGraph.createNode(nodeType);
        typeGraph.createArc(arcType, tgN1, tgN2);

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
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("MatchTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        Graph typeGraph = gra.createTypeGraph();
        typeGraph.createNode(nodeType);

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
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleSchemeTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        Graph typeGraph = gra.createTypeGraph();
        typeGraph.createNode(nodeType);

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
        GraGra gra = BaseFactory.theFactory().createGraGra(true);
        gra.setName("RuleSequenceTest");

        Type nodeType = gra.createNodeType(false);
        nodeType.setStringRepr("Node");

        // Type graph
        Graph typeGraph = gra.createTypeGraph();
        typeGraph.createNode(nodeType);

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
