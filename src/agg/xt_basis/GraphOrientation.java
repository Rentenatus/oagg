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
 *
 * @author jRent
 */
public interface GraphOrientation {

    /**
     *
     * @param anArc
     */
    void targetRemoveArc(final Arc anArc);

    /**
     *
     * @param anArc
     */
    void sourceRemoveArc(final Arc anArc);

    /**
     * Returns an error if the type multiplicity check failed after an edge of
     * the specified type would be created, otherwise - null.
     *
     * @param g
     * @param edgeType
     * @param src
     * @param tar
     * @param currentTypeGraphLevel
     * @return
     */
    public TypeError canCreateArc(final Graph g,
            final Type edgeType,
            final Node src,
            final Node tar,
            int currentTypeGraphLevel
    );

    /**
     *
     * @param g
     * @param edgeType
     * @param src
     * @param tar
     * @return
     */
    public Arc getTypeGraphArc(final Graph g, Type edgeType, Node src, Node tar);

    /**
     *
     * @param g
     * @param edgeType
     * @param src
     * @param tar
     * @return
     */
    public boolean isParallelArcAllowed(final Graph g, Type edgeType, Node src, Node tar);

    /**
     *
     * @param arc
     * @return
     */
    public String[] arcStringKeys(final Arc arc);

    public String[] arcStringKeys(final Type srcParent, final Arc arc, final Type tarParent);

    /**
     * @param anArc
     * @param typeArc
     * @return
     */
    public boolean isUsingArcType(Arc anArc, Arc typeArc);

    /**
     * Creates a new arc for this orientation.
     *
     * @param context the graph context
     * @param type the arc type
     * @param src the source node
     * @param tar the target node
     * @return a new arc instance
     */
    Arc createArc(Graph context, Type type, Node src, Node tar);

    /**
     * Returns whether this orientation is directed.
     *
     * @return true for directed, false for undirected
     */
    boolean isDirected();

    /**
     * Adds an arc to its source and target nodes according to this orientation.
     * For directed graphs: adds to source.outgoing and target.incoming.
     * For undirected graphs: adds to both source.outgoing and target.outgoing.
     *
     * @param arc the arc to add
     * @param source the source node
     * @param target the target node
     */
    void addArcToNodes(Arc arc, Node source, Node target);

    /**
     * Removes an arc from its source and target nodes according to this orientation.
     * For directed graphs: removes from source.outgoing and target.incoming.
     * For undirected graphs: removes from both source.outgoing and target.outgoing.
     *
     * @param arc the arc to remove
     * @param source the source node
     * @param target the target node
     */
    void removeArcFromNodes(Arc arc, Node source, Node target);

    /**
     * Generates the inverse key for an arc. For directed graphs, this returns
     * the same as convertToKey(). For undirected graphs, this returns the
     * reversed key (target->type->source).
     *
     * @param arc the arc to generate the inverse key for
     * @return the inverse key string
     */
    String getInverseArcKey(Arc arc);

    /**
     * Validates if an arc of the specified type can be created between the given nodes.
     * This method combines type graph checks with orientation-specific parallel arc checks.
     *
     * @param g the graph context
     * @param edgeType the type of the edge to create
     * @param src the source node
     * @param tar the target node
     * @return null if valid, TypeError otherwise
     */
    TypeError validateArcCreation(Graph g, Type edgeType, Node src, Node tar);
}
