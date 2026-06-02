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

public class GraphOrientationDirected implements GraphOrientation {

    public final static GraphOrientation INSTANCE = new GraphOrientationDirected();
 
    /**
     *
     * @param anArc
     */
    @Override
    public void sourceRemoveArc(final Arc anArc) {
        // remove arc from its source / target
        ((Node) anArc.getSource()).removeOut(anArc);
    }

    /**
     *
     * @param anArc
     */
    @Override
    public void targetRemoveArc(Arc anArc) {
        ((Node) anArc.getTarget()).removeIn(anArc);
    }

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
    @Override
    public TypeError canCreateArc(final Graph g,
            final Type edgeType,
            final Node source,
            final Node target,
            int currentTypeGraphLevel) {
        return g.itsTypes.canCreateArc(g, edgeType, source, target, currentTypeGraphLevel);
    }

    @Override
    public Arc getTypeGraphArc(final Graph g, Type edgeType, Node src, Node tar) {
        return g.itsTypes.getTypeGraphArc(edgeType, src.getType(), tar.getType());
    }

    public boolean isParallelArcAllowed(final Graph g, Type edgeType, Node src, Node tar) {
        return g.itsTypes.isArcParallel()
                || (src.getOutgoingArc(edgeType, tar) == null);
    }

    @Override
    public String[] arcStringKeys(final Arc arc) {
        String[] keystr = new String[1];
        keystr[0] = arc.convertToKey();
        return keystr;
    }

    @Override
    public String[] arcStringKeys(final Type srcParent, final Arc arc, final Type tarParent) {
        String[] keystr = new String[1];
        keystr[0] = srcParent.convertToKey()
                + arc.getType().convertToKey()
                + tarParent.convertToKey();
        return keystr;
    }

    /**
     * @param anArc
     * @param typeArc
     * @return
     */
    @Override
    public boolean isUsingArcType(Arc anArc, Arc typeArc) {
        return anArc.getType().compareTo(typeArc.getType())
                && ((anArc.getSource().getType().compareTo(typeArc.getSource().getType()))
                || (anArc.getSource().getType().isChildOf(typeArc.getSource().getType())))
                && ((anArc.getTarget().getType().compareTo(typeArc.getTarget().getType()))
                || (anArc.getTarget().getType().isChildOf(typeArc.getTarget().getType())));
    }

    @Override
    public Arc createArc(Graph context, Type type, Node src, Node tar) {
        return new Arc(type, src, tar, context);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addArcToNodes(Arc arc, Node source, Node target) {
        source.addOut(arc);
        target.addIn(arc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeArcFromNodes(Arc arc, Node source, Node target) {
        source.removeOut(arc);
        target.removeIn(arc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInverseArcKey(Arc arc) {
        // For directed graphs, inverse key is the same as the forward key
        // (since direction matters, there is no symmetry)
        return arc.convertToKey();
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
