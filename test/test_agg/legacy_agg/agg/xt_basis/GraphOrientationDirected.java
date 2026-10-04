/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

/**
 * Graph orientation strategy for directed graphs.
 * In directed graphs, arcs have a clear direction from source to target.
 *
 * @author Janusch Rentenatus
 */
public class GraphOrientationDirected extends AbstractGraphOrientation {

    /**
     * Singleton instance for directed graph orientation.
     */
    public static final GraphOrientation INSTANCE = new GraphOrientationDirected();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private GraphOrientationDirected() {
        // Singleton - private constructor
    }

    /**
     * Removes the arc from its source node.
     * In directed graphs, removes from source's outgoing arcs.
     *
     * @param arc the arc to remove
     */
    @Override
    public void sourceRemoveArc(final Arc arc) {
        // Remove arc from its source / target
        ((Node) arc.getSource()).removeOut(arc);
    }

    /**
     * Removes the arc from its target node.
     * In directed graphs, removes from target's incoming arcs.
     *
     * @param arc the arc to remove
     */
    @Override
    public void targetRemoveArc(final Arc arc) {
        ((Node) arc.getTarget()).removeIn(arc);
    }

    /**
     * Returns an error if the type multiplicity check failed after an edge of
     * the specified type would be created, otherwise returns null.
     *
     * @param graph the graph context
     * @param edgeType the type of the edge
     * @param source the source node
     * @param target the target node
     * @param currentTypeGraphLevel the current type graph check level
     * @return TypeError if validation fails, null otherwise
     */
    @Override
    public TypeError canCreateArc(final Graph graph, final Type edgeType,
            final Node source, final Node target, final int currentTypeGraphLevel) {
        return graph.itsTypes.canCreateArc(graph, edgeType, source, target, currentTypeGraphLevel);
    }

    /**
     * Gets the type graph arc for the specified edge type and nodes.
     * For directed graphs, looks up the arc in the type graph with matching source and target types.
     *
     * @param graph the graph context
     * @param edgeType the edge type to look up
     * @param source the source node
     * @param target the target node
     * @return the type graph arc, or null if not found
     */
    @Override
    public Arc getTypeGraphArc(final Graph graph, final Type edgeType, final Node source, final Node target) {
        return graph.itsTypes.getTypeGraphArc(edgeType, source.getType(), target.getType());
    }

    /**
     * Checks if a parallel arc is allowed between the specified nodes.
     * For directed graphs, checks if an outgoing arc of the same type already exists.
     *
     * @param graph the graph context
     * @param edgeType the edge type
     * @param source the source node
     * @param target the target node
     * @return true if parallel arcs are allowed, false otherwise
     */
    @Override
    public boolean isParallelArcAllowed(final Graph graph, final Type edgeType, 
            final Node source, final Node target) {
        return graph.itsTypes.isArcParallel()
                || (source.getOutgoingArc(edgeType, target) == null);
    }

    /**
     * Generates string keys for the specified arc.
     * For directed graphs, returns a single key representing the arc.
     *
     * @param arc the arc
     * @return array of string keys (length 1 for directed graphs)
     */
    @Override
    public String[] arcStringKeys(final Arc arc) {
        final String[] keyStrings = new String[1];
        keyStrings[0] = arc.convertToKey();
        return keyStrings;
    }

    /**
     * Generates string keys for the specified arc with parent types.
     * For directed graphs, returns a single key with parent types.
     *
     * @param sourceParent the source parent type
     * @param arc the arc
     * @param targetParent the target parent type
     * @return array of string keys (length 1 for directed graphs)
     */
    @Override
    public String[] arcStringKeys(final Type sourceParent, final Arc arc, final Type targetParent) {
        final String[] keyStrings = new String[1];
        keyStrings[0] = sourceParent.convertToKey()
                + arc.getType().convertToKey()
                + targetParent.convertToKey();
        return keyStrings;
    }

    /**
     * Checks if the specified arc uses the specified type arc.
     * For directed graphs, checks type compatibility with direction.
     *
     * @param arc the arc to check
     * @param typeArc the type arc to compare against
     * @return true if the arc uses the type arc, false otherwise
     */
    @Override
    public boolean isUsingArcType(final Arc arc, final Arc typeArc) {
        return arc.getType().compareTo(typeArc.getType())
                && ((arc.getSource().getType().compareTo(typeArc.getSource().getType()))
                || (arc.getSource().getType().isChildOf(typeArc.getSource().getType())))
                && ((arc.getTarget().getType().compareTo(typeArc.getTarget().getType()))
                || (arc.getTarget().getType().isChildOf(typeArc.getTarget().getType())));
    }

    /**
     * Creates a new arc for directed graphs.
     *
     * @param context the graph context
     * @param type the arc type
     * @param source the source node
     * @param target the target node
     * @return a new arc instance
     */
    @Override
    public Arc createArc(final Graph context, final Type type, final Node source, final Node target) {
        return new Arc(type, source, target, context);
    }

    /**
     * Returns whether this orientation is directed.
     *
     * @return true (this is a directed orientation)
     */
    @Override
    public boolean isDirected() {
        return true;
    }

    /**
     * Adds an arc to its source and target nodes.
     * For directed graphs, adds to source's outgoing arcs and target's incoming arcs.
     *
     * @param arc the arc to add
     * @param source the source node
     * @param target the target node
     */
    @Override
    public void addArcToNodes(final Arc arc, final Node source, final Node target) {
        source.addOut(arc);
        target.addIn(arc);
    }

    /**
     * Removes an arc from its source and target nodes.
     * For directed graphs, removes from source's outgoing arcs and target's incoming arcs.
     *
     * @param arc the arc to remove
     * @param source the source node
     * @param target the target node
     */
    @Override
    public void removeArcFromNodes(final Arc arc, final Node source, final Node target) {
        source.removeOut(arc);
        target.removeIn(arc);
    }

    /**
     * Generates the inverse key for an arc.
     * For directed graphs, the inverse key is the same as the forward key
     * (since direction matters, there is no symmetry).
     *
     * @param arc the arc to generate the inverse key for
     * @return the inverse key string (same as convertToKey for directed graphs)
     */
    @Override
    public String getInverseArcKey(final Arc arc) {
        // For directed graphs, inverse key is the same as the forward key
        // (since direction matters, there is no symmetry)
        return arc.convertToKey();
    }
}
