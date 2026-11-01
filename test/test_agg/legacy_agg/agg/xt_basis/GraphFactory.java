/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

/**
 * Factory class for creating graph elements (Graphs, Nodes, Arcs). This class provides a centralized way to create
 * graph objects with consistent configuration and validation.
 *
 * @author Janusch Rentenatus
 */
public final class GraphFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private GraphFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a new graph with the specified orientation.
     *
     * @param orientation the graph orientation (directed or undirected)
     * @return a new graph instance
     */
    public static Graph createGraph(GraphOrientation orientation) {
        return new Graph(orientation);
    }

    /**
     * Creates a new graph with the specified orientation and type set.
     *
     * @param orientation the graph orientation
     * @param typeSet the type set to use
     * @return a new graph instance
     */
    public static Graph createGraph(GraphOrientation orientation, TypeSet typeSet) {
        return new Graph(orientation, typeSet);
    }

    /**
     * Creates a new directed graph.
     *
     * @return a new directed graph instance
     */
    public static Graph createDirectedGraph() {
        return new Graph(GraphOrientationDirected.INSTANCE);
    }

    /**
     * Creates a new directed graph with the specified type set.
     *
     * @param typeSet the type set to use
     * @return a new directed graph instance
     */
    public static Graph createDirectedGraph(TypeSet typeSet) {
        return new Graph(GraphOrientationDirected.INSTANCE, typeSet);
    }

    /**
     * Creates a new undirected graph.
     *
     * @return a new undirected graph instance
     */
    public static Graph createUndirectedGraph() {
        return new Graph(GraphOrientationUndirected.INSTANCE);
    }

    /**
     * Creates a new undirected graph with the specified type set.
     *
     * @param typeSet the type set to use
     * @return a new undirected graph instance
     */
    public static Graph createUndirectedGraph(TypeSet typeSet) {
        return new Graph(GraphOrientationUndirected.INSTANCE, typeSet);
    }

    /**
     * Creates a new node of the specified type in the given graph context.
     *
     * @param nodeType the type for the new node
     * @param graph the graph context
     * @return a new node instance
     */
    public static Node createNode(Type nodeType, Graph graph) {
        return new Node(nodeType, graph);
    }

    /**
     * Creates a new arc of the specified type between source and target nodes.
     *
     * @param arcType the type for the new arc
     * @param source the source node
     * @param target the target node
     * @param graph the graph context
     * @return a new arc instance
     */
    public static Arc createArc(Type arcType, Node source, Node target, Graph graph) {
        return new Arc(arcType, source, target, graph);
    }

    /**
     * Creates a new attribute instance for the specified attribute type and context.
     *
     * @param attrType the attribute type
     * @param attrContext the attribute context
     * @return a new attribute instance
     */
    public static agg.attribute.AttrInstance createAttributeInstance(
            agg.attribute.AttrType attrType,
            agg.attribute.AttrContext attrContext) {
        if (attrType != null && attrContext != null) {
            return agg.attribute.impl.AttrTupleManager.getDefaultManager().newInstance(
                    attrType, attrContext);
        }
        return null;
    }

    /**
     * Creates a new type set.
     *
     * @return a new type set instance
     */
    public static TypeSet createTypeSet() {
        return new TypeSet();
    }
}
