/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import agg.xt_basis.calculator.GraphOrientation;

public class GraphOrientationUndirected implements GraphOrientation {

    public final static GraphOrientation INSTANCE = new GraphOrientationUndirected();

    /**
     *
     * @param anArc
     */
    @Override
    public void targetRemoveArc(final Arc anArc) {
        ((Node) anArc.getTarget()).removeOut(anArc);
    }

    /**
     *
     * @param anArc
     */
    @Override
    public void sourceRemoveArc(final Arc anArc) {
        ((Node) anArc.getSource()).removeOut(anArc);
    }

    /**
     * Returns an error if the type multiplicity check failed after an edge of
     * the specified type would be created, otherwise - null.
     *
     * @param g
     * @param edgeType
     * @param source
     * @param target
     * @param currentTypeGraphLevel
     * @return
     */
    @Override
    public TypeError canCreateArc(final Graph g,
            final Type edgeType,
            final Node source,
            final Node target,
            int currentTypeGraphLevel) {
        // check source->target already exists
        TypeError error = g.itsTypes.canCreateArc(g, edgeType, source, target, currentTypeGraphLevel);
        // check target->source already exists
        if (error != null) {
            return error;
        }
        return g.itsTypes.canCreateArc(g, edgeType, target, source, currentTypeGraphLevel);
    }

    @Override
    public Arc getTypeGraphArc(final Graph g, Type edgeType, Node src, Node tar) {
        Arc typearc = g.itsTypes.getTypeGraphArc(edgeType, src.getType(), tar.getType());
        if (typearc != null) {
            return typearc;
        }
        return g.itsTypes.getTypeGraphArc(edgeType, tar.getType(), src.getType());
    }

    @Override
    public boolean isParallelArcAllowed(final Graph g, Type edgeType, Node src, Node tar) {
        return g.itsTypes.isArcParallel()
                || (src.getOutgoingArc(edgeType, tar) == null
                && tar.getOutgoingArc(edgeType, src) == null);
    }

    @Override
    public String[] arcStringKeys(final Arc arc) {
        String[] keystr = new String[2];
        keystr[0] = arc.convertToKey();
        keystr[1] = getInverseArcKey(arc);
        return keystr;
    }

    @Override
    public String[] arcStringKeys(final Type srcParent, final Arc arc, final Type tarParent) {
        String[] keystr = new String[2];
        keystr[0] = srcParent.convertToKey()
                + arc.getType().convertToKey()
                + tarParent.convertToKey();
        keystr[1] = tarParent.convertToKey()
                + arc.getType().convertToKey()
                + srcParent.convertToKey();
        return keystr;
    }

    /**
     * @param anArc
     * @param typeArc
     * @return
     */
    @Override
    public boolean isUsingArcType(Arc anArc, Arc typeArc) {
        if (anArc.getType().compareTo(typeArc.getType())) {
            if (((anArc.getSource().getType().compareTo(((Arc) typeArc).getSource().getType()))
                    || (anArc.getSource().getType().isChildOf(((Arc) typeArc).getSource().getType())))
                    && ((anArc.getTarget().getType().compareTo(((Arc) typeArc).getTarget().getType()))
                    || (anArc.getTarget().getType().isChildOf(((Arc) typeArc).getTarget().getType())))) {
                return true;
            } else if (((anArc.getTarget().getType().compareTo(((Arc) typeArc).getSource().getType()))
                    || (anArc.getTarget().getType().isChildOf(((Arc) typeArc).getSource().getType())))
                    && ((anArc.getSource().getType().compareTo(((Arc) typeArc).getTarget().getType()))
                    || (anArc.getSource().getType().isChildOf(((Arc) typeArc).getTarget().getType())))) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc createArc(Graph context, Type type, Node src, Node tar) {
        return new Arc(type, src, tar, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirected() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addArcToNodes(Arc arc, Node source, Node target) {
        // In undirected graphs, both nodes store the arc in their outgoing arcs
        source.addOut(arc);
        target.addOut(arc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeArcFromNodes(Arc arc, Node source, Node target) {
        // In undirected graphs, both nodes store the arc in their outgoing arcs
        source.removeOut(arc);
        target.removeOut(arc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInverseArcKey(Arc arc) {
        // For undirected graphs, the inverse key is target->type->source
        return arc.getTarget().getType().convertToKey()
                .concat(arc.getType().convertToKey())
                .concat(arc.getSource().getType().convertToKey());
    }

    @Override
    public TypeError validateArcCreation(Graph g, Type edgeType, Node src, Node tar) {
        if (g.getTypeSet().getTypeGraph() == null
                || g.getTypeSet().getLevelOfTypeGraphCheck() == TypeSet.DISABLED
                || g.getTypeSet().getLevelOfTypeGraphCheck() == TypeSet.ENABLED_INHERITANCE) {
            if (isParallelArcAllowed(g, edgeType, src, tar)) {
                return null;
            }
            return new TypeError(TypeError.NO_PARALLEL_ARC,
                    "No parallel edges allowed");
        }
        Arc typearc = getTypeGraphArc(g, edgeType, src, tar);
        if (typearc != null) {
            if (isParallelArcAllowed(g, edgeType, src, tar)) {
                return null;
            }
            return new TypeError(TypeError.NO_PARALLEL_ARC,
                    "No parallel edges allowed");
        }
        return new TypeError(TypeError.NO_SUCH_TYPE,
                "The edge of the type \"" + edgeType.getName()
                + "\" is not allowed between node types \""
                + src.getType().getName() + "\"  and  \""
                + tar.getType().getName() + "\".");
    }

}
