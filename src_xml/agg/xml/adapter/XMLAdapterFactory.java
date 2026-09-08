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
import agg.xt_basis.Node;
import agg.xt_basis.Rule;
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
        
        // Check for specific types and return appropriate adapter
        if (xmlObject instanceof Graph) {
            return new GraphAdapter((Graph) xmlObject);
        } else if (xmlObject instanceof Node) {
            return new NodeAdapter((Node) xmlObject);
        } else if (xmlObject instanceof Arc) {
            return new ArcAdapter((Arc) xmlObject);
        } else if (xmlObject instanceof Rule) {
            return new RuleAdapter((Rule) xmlObject);
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
