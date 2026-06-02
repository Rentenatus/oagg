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
 * Graph orientation strategy for undirected graphs.
 * In undirected graphs, arcs have no direction - they are stored as outgoing
 * arcs in both connected nodes.
 *
 * @author Janusch Rentenatus
 */
public class GraphOrientationUndirected extends AbstractGraphOrientation {

    /**
     * Singleton instance for undirected graph orientation.
     */
    public static final GraphOrientation INSTANCE = new GraphOrientationUndirected();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private GraphOrientationUndirected() {
        // Singleton - private constructor
    }

    /**
     * Removes the arc from its source node.
     * In undirected graphs, removes from source's outgoing arcs.
     *
     * @param arc the arc to remove
     */
    @Override
    public void sourceRemoveArc(final Arc arc) {
        ((Node) arc.getSource()).removeOut(arc);
    }

    /**
     * Removes the arc from its target node.
     * In undirected graphs, removes from target's outgoing arcs.
     *
     * @param arc the arc to remove
     */
    @Override
    public void targetRemoveArc(final Arc arc) {
        ((Node) arc.getTarget()).removeOut(arc);
    }

    /**
     * Returns an error if the type multiplicity check failed after an edge of
     * the specified type would be created, otherwise returns null.
     * For undirected graphs, checks both directions (source->target and target->source).
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
        // Check source->target already exists
        TypeError error = graph.itsTypes.canCreateArc(graph, edgeType, source, target, currentTypeGraphLevel);
        // Check target->source already exists
        if (error != null) {
            return error;
        }
        return graph.itsTypes.canCreateArc(graph, edgeType, target, source, currentTypeGraphLevel);
    }

    /**
     * Gets the type graph arc for the specified edge type and nodes.
     * For undirected graphs, looks up the arc in both directions (source->target and target->source).
     *
     * @param graph the graph context
     * @param edgeType the edge type to look up
     * @param source the source node
     * @param target the target node
     * @return the type graph arc, or null if not found
     */
    @Override
    public Arc getTypeGraphArc(final Graph graph, final Type edgeType, final Node source, final Node target) {
        Arc typeArc = graph.itsTypes.getTypeGraphArc(edgeType, source.getType(), target.getType());
        if (typeArc != null) {
            return typeArc;
        }
        return graph.itsTypes.getTypeGraphArc(edgeType, target.getType(), source.getType());
    }

    /**
     * Checks if a parallel arc is allowed between the specified nodes.
     * For undirected graphs, checks if arcs exist in both directions.
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
                || (source.getOutgoingArc(edgeType, target) == null
                && target.getOutgoingArc(edgeType, source) == null);
    }

    /**
     * Generates string keys for the specified arc.
     * For undirected graphs, returns two keys: one for each direction.
     *
     * @param arc the arc
     * @return array of string keys (length 2 for undirected graphs)
     */
    @Override
    public String[] arcStringKeys(final Arc arc) {
        final String[] keyStrings = new String[2];
        keyStrings[0] = arc.convertToKey();
        keyStrings[1] = getInverseArcKey(arc);
        return keyStrings;
    }

    /**
     * Generates string keys for the specified arc with parent types.
     * For undirected graphs, returns two keys: one for each direction with parent types.
     *
     * @param sourceParent the source parent type
     * @param arc the arc
     * @param targetParent the target parent type
     * @return array of string keys (length 2 for undirected graphs)
     */
    @Override
    public String[] arcStringKeys(final Type sourceParent, final Arc arc, final Type targetParent) {
        final String[] keyStrings = new String[2];
        keyStrings[0] = sourceParent.convertToKey()
                + arc.getType().convertToKey()
                + targetParent.convertToKey();
        keyStrings[1] = targetParent.convertToKey()
                + arc.getType().convertToKey()
                + sourceParent.convertToKey();
        return keyStrings;
    }

    /**
     * Checks if the specified arc uses the specified type arc.
     * For undirected graphs, checks type compatibility in both directions.
     *
     * @param arc the arc to check
     * @param typeArc the type arc to compare against
     * @return true if the arc uses the type arc, false otherwise
     */
    @Override
    public boolean isUsingArcType(final Arc arc, final Arc typeArc) {
        if (arc.getType().compareTo(typeArc.getType())) {
            // Check direction: arc.source -> arc.target matches typeArc.source -> typeArc.target
            if (((arc.getSource().getType().compareTo(typeArc.getSource().getType()))
                    || (arc.getSource().getType().isChildOf(typeArc.getSource().getType())))
                    && ((arc.getTarget().getType().compareTo(typeArc.getTarget().getType()))
                    || (arc.getTarget().getType().isChildOf(typeArc.getTarget().getType())))) {
                return true;
            }
            // Check reverse direction: arc.source -> arc.target matches typeArc.target -> typeArc.source
            if (((arc.getTarget().getType().compareTo(typeArc.getSource().getType()))
                    || (arc.getTarget().getType().isChildOf(typeArc.getSource().getType())))
                    && ((arc.getSource().getType().compareTo(typeArc.getTarget().getType()))
                    || (arc.getSource().getType().isChildOf(typeArc.getTarget().getType())))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new arc for undirected graphs.
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
     * @return false (this is an undirected orientation)
     */
    @Override
    public boolean isDirected() {
        return false;
    }

    /**
     * Adds an arc to its source and target nodes.
     * For undirected graphs, adds to both source's and target's outgoing arcs.
     *
     * @param arc the arc to add
     * @param source the source node
     * @param target the target node
     */
    @Override
    public void addArcToNodes(final Arc arc, final Node source, final Node target) {
        // In undirected graphs, both nodes store the arc in their outgoing arcs
        source.addOut(arc);
        target.addOut(arc);
    }

    /**
     * Removes an arc from its source and target nodes.
     * For undirected graphs, removes from both source's and target's outgoing arcs.
     *
     * @param arc the arc to remove
     * @param source the source node
     * @param target the target node
     */
    @Override
    public void removeArcFromNodes(final Arc arc, final Node source, final Node target) {
        // In undirected graphs, both nodes store the arc in their outgoing arcs
        source.removeOut(arc);
        target.removeOut(arc);
    }

    /**
     * Generates the inverse key for an arc.
     * For undirected graphs, the inverse key is target->type->source.
     *
     * @param arc the arc to generate the inverse key for
     * @return the inverse key string (target->type->source)
     */
    @Override
    public String getInverseArcKey(final Arc arc) {
        // For undirected graphs, the inverse key is target->type->source
        return arc.getTarget().getType().convertToKey()
                .concat(arc.getType().convertToKey())
                .concat(arc.getSource().getType().convertToKey());
    }
}
