/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;
import agg.xt_basis.Arc;
import agg.xt_basis.Graph;
import agg.xt_basis.GraGra;
import agg.xt_basis.Match;
import agg.xt_basis.Node;
import agg.xt_basis.Rule;
import agg.xt_basis.TypeGraph;
import agg.xt_basis.TypeImpl;
import agg.xt_basis.NodeTypeImpl;
import agg.xt_basis.ArcTypeImpl;
import agg.xt_basis.agt.MultiRule;
import agg.xt_basis.agt.RuleScheme;
import agg.attribute.impl.ValueTuple;
import agg.attribute.impl.VarTuple;
import agg.attribute.impl.CondTuple;
import agg.attribute.impl.DeclTuple;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.DeclMember;
import agg.cons.Formula;
import agg.cons.AtomConstraint;
import agg.parser.ConflictsDependenciesContainer;
import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.parser.LayerFunction;
import agg.parser.LayeredDependencyPairContainer;
import agg.parser.LayeredExcludePairContainer;
import agg.parser.PriorityDependencyPairContainer;
import agg.parser.PriorityExcludePairContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.xml.core.XMLSerializable;

/**
 * Factory class for creating XML adapters for AGG domain objects.
 * This factory provides methods to create appropriate adapters for different
 * types of domain objects that implement XMLObject.
 */
public class XMLAdapterFactory {
    
    /**
     * Creates an appropriate adapter for the specified XMLObject.
     * The factory will create a specific adapter based on the actual type
     * of the object, or a generic XMLObjectAdapter if no specific adapter exists.
     * 
     * @param xmlObject The XMLObject to adapt
     * @return An XMLSerializable adapter for the object
     */
    public static XMLSerializable createAdapter(XMLObject xmlObject) {
        if (xmlObject == null) {
            return null;
        }
        
        // Check for specific types and return appropriate adapter.
        // Order matters: most-specific subtypes must be checked before their supertypes.
        if (xmlObject instanceof GraGra) {
            return new GraGraAdapter((GraGra) xmlObject);
        } else if (xmlObject instanceof TypeGraph) {
            return new TypeGraphAdapter((TypeGraph) xmlObject);
        } else if (xmlObject instanceof Graph) {
            return new GraphAdapter((Graph) xmlObject);
        } else if (xmlObject instanceof Node) {
            return new NodeAdapter((Node) xmlObject);
        } else if (xmlObject instanceof Arc) {
            return new ArcAdapter((Arc) xmlObject);
        } else if (xmlObject instanceof RuleScheme) {
            return new RuleSchemeAdapter((RuleScheme) xmlObject);
        } else if (xmlObject instanceof MultiRule) {
            return new RuleAdapter((MultiRule) xmlObject);
        } else if (xmlObject instanceof Rule) {
            return new RuleAdapter((Rule) xmlObject);
        } else if (xmlObject instanceof Match) {
            return new MatchAdapter((Match) xmlObject);
        } else if (xmlObject instanceof NodeTypeImpl) {
            return new NodeTypeImplAdapter((NodeTypeImpl) xmlObject);
        } else if (xmlObject instanceof ArcTypeImpl) {
            return new ArcTypeImplAdapter((ArcTypeImpl) xmlObject);
        } else if (xmlObject instanceof TypeImpl) {
            return new TypeImplAdapter((TypeImpl) xmlObject);
        } else if (xmlObject instanceof ValueTuple) {
            return new ValueTupleAdapter((ValueTuple) xmlObject);
        } else if (xmlObject instanceof VarTuple) {
            return new AttributeAdapters.VarTupleAdapter((VarTuple) xmlObject);
        } else if (xmlObject instanceof CondTuple) {
            return new AttributeAdapters.CondTupleAdapter((CondTuple) xmlObject);
        } else if (xmlObject instanceof DeclTuple) {
            return new AttributeAdapters.DeclTupleAdapter((DeclTuple) xmlObject);
        } else if (xmlObject instanceof ValueMember) {
            return new AttributeAdapters.ValueMemberAdapter((ValueMember) xmlObject);
        } else if (xmlObject instanceof CondMember) {
            return new AttributeAdapters.CondMemberAdapter((CondMember) xmlObject);
        } else if (xmlObject instanceof DeclMember) {
            return new AttributeAdapters.DeclMemberAdapter((DeclMember) xmlObject);
        } else if (xmlObject instanceof Formula) {
            return new FormulaAdapter((Formula) xmlObject);
        } else if (xmlObject instanceof AtomConstraint) {
            return new AtomConstraintAdapter((AtomConstraint) xmlObject);
        } else if (xmlObject instanceof ApplRuleSequence) {
            return new ApplRuleSequenceAdapter((ApplRuleSequence) xmlObject);
        } else if (xmlObject instanceof ConflictsDependenciesContainer) {
            return new ConflictsDependenciesContainerAdapter((ConflictsDependenciesContainer) xmlObject);
        } else if (xmlObject instanceof LayeredDependencyPairContainer) {
            return new ParserAdapters.LayeredDependencyPairContainerAdapter((LayeredDependencyPairContainer) xmlObject);
        } else if (xmlObject instanceof LayeredExcludePairContainer) {
            return new ParserAdapters.LayeredExcludePairContainerAdapter((LayeredExcludePairContainer) xmlObject);
        } else if (xmlObject instanceof PriorityDependencyPairContainer) {
            return new ParserAdapters.PriorityDependencyPairContainerAdapter((PriorityDependencyPairContainer) xmlObject);
        } else if (xmlObject instanceof PriorityExcludePairContainer) {
            return new ParserAdapters.PriorityExcludePairContainerAdapter((PriorityExcludePairContainer) xmlObject);
        } else if (xmlObject instanceof DependencyPairContainer) {
            return new ParserAdapters.DependencyPairContainerAdapter((DependencyPairContainer) xmlObject);
        } else if (xmlObject instanceof ExcludePairContainer) {
            return new ParserAdapters.ExcludePairContainerAdapter((ExcludePairContainer) xmlObject);
        } else if (xmlObject instanceof LayerFunction) {
            return new ParserAdapters.LayerFunctionAdapter((LayerFunction) xmlObject);
        }
        
        // Fall back to generic adapter
        return new XMLObjectAdapter(xmlObject);
    }
    
    /**
     * Creates a GraphAdapter for the specified Graph.
     * 
     * @param graph The Graph to adapt
     * @return A GraphAdapter instance
     */
    public static GraphAdapter createGraphAdapter(Graph graph) {
        return new GraphAdapter(graph);
    }
    
    /**
     * Creates a NodeAdapter for the specified Node.
     * 
     * @param node The Node to adapt
     * @return A NodeAdapter instance
     */
    public static NodeAdapter createNodeAdapter(Node node) {
        return new NodeAdapter(node);
    }
    
    /**
     * Creates an ArcAdapter for the specified Arc.
     * 
     * @param arc The Arc to adapt
     * @return An ArcAdapter instance
     */
    public static ArcAdapter createArcAdapter(Arc arc) {
        return new ArcAdapter(arc);
    }
    
    /**
     * Creates a RuleAdapter for the specified Rule.
     * 
     * @param rule The Rule to adapt
     * @return A RuleAdapter instance
     */
    public static RuleAdapter createRuleAdapter(Rule rule) {
        return new RuleAdapter(rule);
    }
    
    /**
     * Creates a generic XMLObjectAdapter for the specified XMLObject.
     * 
     * @param xmlObject The XMLObject to adapt
     * @return An XMLObjectAdapter instance
     */
    public static XMLObjectAdapter createXMLObjectAdapter(XMLObject xmlObject) {
        return new XMLObjectAdapter(xmlObject);
    }
}
