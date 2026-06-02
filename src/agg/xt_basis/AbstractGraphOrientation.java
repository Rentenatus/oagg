/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import agg.xt_basis.Arc;
import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xt_basis.Type;
import agg.xt_basis.TypeError;
import agg.xt_basis.TypeSet;

/**
 * Abstract base class for graph orientation strategies.
 * Contains common logic for directed and undirected graph orientations.
 *
 * @author Janusch Rentenatus
 */
public abstract class AbstractGraphOrientation implements GraphOrientation {

    /**
     * Protected constructor to enforce extension only within package.
     */
    protected AbstractGraphOrientation() {
        // Abstract base class - constructor is protected
    }

    /**
     * Creates a new arc for this orientation. Both directed and undirected
     * orientations use the same arc creation logic.
     *
     * @param context the graph context
     * @param type the arc type
     * @param src the source node
     * @param tar the target node
     * @return a new arc instance
     */
    @Override
    public Arc createArc(final Graph context, final Type type, final Node src, final Node tar) {
        return new Arc(type, src, tar, context);
    }

    /**
     * Validates if an arc of the specified type can be created between the given nodes.
     * This method provides common validation logic used by both directed and undirected orientations.
     *
     * @param graph the graph context
     * @param edgeType the type of the edge to create
     * @param source the source node
     * @param target the target node
     * @return null if valid, TypeError otherwise
     */
    @Override
    public TypeError validateArcCreation(final Graph graph, final Type edgeType, 
            final Node source, final Node target) {
        if (graph.getTypeSet().getTypeGraph() == null
                || graph.getTypeSet().getLevelOfTypeGraphCheck() == TypeSet.DISABLED
                || graph.getTypeSet().getLevelOfTypeGraphCheck() == TypeSet.ENABLED_INHERITANCE) {
            if (isParallelArcAllowed(graph, edgeType, source, target)) {
                return null;
            }
            return new TypeError(TypeError.NO_PARALLEL_ARC, "No parallel edges allowed");
        }
        Arc typeArc = getTypeGraphArc(graph, edgeType, source, target);
        if (typeArc != null) {
            if (isParallelArcAllowed(graph, edgeType, source, target)) {
                return null;
            }
            return new TypeError(TypeError.NO_PARALLEL_ARC, "No parallel edges allowed");
        }
        return new TypeError(TypeError.NO_SUCH_TYPE,
                "The edge of the type \"" + edgeType.getName()
                + "\" is not allowed between node types \""
                + source.getType().getName() + "\" and \""
                + target.getType().getName() + "\".");
    }

    /**
     * Returns an error if the type multiplicity check failed after an edge of
     * the specified type would be created, otherwise returns null.
     * Subclasses must implement orientation-specific checks.
     *
     * @param graph the graph context
     * @param edgeType the type of the edge
     * @param source the source node
     * @param target the target node
     * @param currentTypeGraphLevel the current type graph check level
     * @return TypeError if validation fails, null otherwise
     */
    @Override
    public abstract TypeError canCreateArc(final Graph graph, final Type edgeType,
            final Node source, final Node target, int currentTypeGraphLevel);

    /**
     * Gets the type graph arc for the specified edge type and nodes.
     * Subclasses must implement orientation-specific lookup.
     *
     * @param graph the graph context
     * @param edgeType the edge type to look up
     * @param source the source node
     * @param target the target node
     * @return the type graph arc, or null if not found
     */
    @Override
    public abstract Arc getTypeGraphArc(final Graph graph, final Type edgeType, 
            final Node source, final Node target);

    /**
     * Checks if a parallel arc is allowed between the specified nodes.
     * Subclasses must implement orientation-specific logic.
     *
     * @param graph the graph context
     * @param edgeType the edge type
     * @param source the source node
     * @param target the target node
     * @return true if parallel arcs are allowed, false otherwise
     */
    @Override
    public abstract boolean isParallelArcAllowed(final Graph graph, final Type edgeType, 
            final Node source, final Node target);

    /**
     * Generates string keys for the specified arc.
     * Subclasses must implement orientation-specific key generation.
     *
     * @param arc the arc
     * @return array of string keys
     */
    @Override
    public abstract String[] arcStringKeys(final Arc arc);

    /**
     * Generates string keys for the specified arc with parent types.
     * Subclasses must implement orientation-specific key generation.
     *
     * @param sourceParent the source parent type
     * @param arc the arc
     * @param targetParent the target parent type
     * @return array of string keys
     */
    @Override
    public abstract String[] arcStringKeys(final Type sourceParent, final Arc arc, final Type targetParent);

    /**
     * Checks if the specified arc uses the specified type arc.
     * Subclasses must implement orientation-specific type checking.
     *
     * @param arc the arc to check
     * @param typeArc the type arc to compare against
     * @return true if the arc uses the type arc, false otherwise
     */
    @Override
    public abstract boolean isUsingArcType(final Arc arc, final Arc typeArc);

    /**
     * Removes the arc from its source node according to this orientation.
     *
     * @param arc the arc to remove
     */
    @Override
    public abstract void sourceRemoveArc(final Arc arc);

    /**
     * Removes the arc from its target node according to this orientation.
     *
     * @param arc the arc to remove
     */
    @Override
    public abstract void targetRemoveArc(final Arc arc);

    /**
     * Returns whether this orientation is directed.
     *
     * @return true for directed orientation, false for undirected
     */
    @Override
    public abstract boolean isDirected();

    /**
     * Adds an arc to its source and target nodes according to this orientation.
     *
     * @param arc the arc to add
     * @param source the source node
     * @param target the target node
     */
    @Override
    public abstract void addArcToNodes(final Arc arc, final Node source, final Node target);

    /**
     * Removes an arc from its source and target nodes according to this orientation.
     *
     * @param arc the arc to remove
     * @param source the source node
     * @param target the target node
     */
    @Override
    public abstract void removeArcFromNodes(final Arc arc, final Node source, final Node target);

    /**
     * Generates the inverse key for an arc.
     *
     * @param arc the arc to generate the inverse key for
     * @return the inverse key string
     */
    @Override
    public abstract String getInverseArcKey(final Arc arc);
}
