/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis.calculator;

import agg.xt_basis.Arc;
import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xt_basis.Type;
import agg.xt_basis.TypeError;
import agg.xt_basis.TypeSet;
import agg.xt_basis.UndirectedArc;

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

}
