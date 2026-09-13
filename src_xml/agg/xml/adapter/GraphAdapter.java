/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Graph;

/**
 * Adapter for Graph objects that implements the new XMLSerializable interface.
 * This adapter wraps a Graph instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class GraphAdapter extends DomainObjectAdapter<Graph> {

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
}
