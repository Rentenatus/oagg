/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import java.util.Iterator;

/**
 *
 * @author Olga
 * @author Janusch Rentenatus
 */
public class UndirectedGraph extends Graph {

    /**
     * Creates an empty graph with an empty TypeSet.
     *
     * Use {@link #Graph(boolean)}, to create a complete graph (a host graph).
     */
    public UndirectedGraph() {
        super(GraphOrientationUndirected.INSTANCE);
    }

    /**
     * Creates an empty graph with the specified TypeSet.Use {@link #Graph(TypeSet, boolean)}, to create a complete
     * graph (a host graph).
     *
     * @param aTypeSet
     */
    public UndirectedGraph(TypeSet aTypeSet) {
        super(GraphOrientationUndirected.INSTANCE, aTypeSet);
    }

    /**
     * Creates an empty graph with the specified TypeSet.
     *
     * @param aTypeSet the TypeSet to use
     * @param completeGraph true, to create a host graph
     */
    public UndirectedGraph(TypeSet aTypeSet, boolean completeGraph) {
        super(GraphOrientationUndirected.INSTANCE, aTypeSet, completeGraph);
    }

    /**
     * Adds the specified edge to my edges.The type of the specified edge has to be in my type set.<br>
     * The edge must be an instance of <code>UndirectedArc</code>.
     *
     * @param anArc
     */
    @Override
    public void addArc(Arc anArc) {
        if (anArc instanceof UndirectedArc) {
            super.addArc(anArc);
        }
    }

    /**
     *
     * @param t
     * @param src
     * @param tar
     * @return
     */
    @Override
    protected UndirectedArc createArcFast(Type t, Node src, Node tar) {
        return new UndirectedArc(t, src, tar, this);
    }

}
