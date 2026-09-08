/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Graph;
import agg.util.XMLHelper;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;

/**
 * Adapter for Graph objects that implements the new XMLSerializable interface.
 * This adapter wraps a Graph instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class GraphAdapter extends DomainObjectAdapter<Graph> implements XMLSerializable {
    
    /**
     * Creates a new adapter for the specified Graph.
     * 
     * @param graph The Graph to adapt
     */
    public GraphAdapter(Graph graph) {
        super(graph);
    }
    
    /**
     * Gets the adapted Graph instance.
     * 
     * @return The Graph
     */
    public Graph getGraph() {
        return getDomainObject();
    }
    
    /**
     * Sets the name of the graph in the XML output.
     * This is a convenience method for setting graph metadata.
     * 
     * @param context The serializer context
     * @param name The graph name
     */
    public void serializeGraphName(XMLSerializerContext context, String name) {
        // This can be extended to handle graph-specific serialization
    }
}
