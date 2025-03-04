/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
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

}
