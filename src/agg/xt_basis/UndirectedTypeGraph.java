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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import agg.attribute.impl.ValueTuple;
import agg.util.Change;

/**
 *
 * @author Olga
 * @author Janusch Rentenatus
 */
public class UndirectedTypeGraph extends TypeGraph {

    public UndirectedTypeGraph(TypeSet aTypeSet) {
        super(GraphOrientationUndirected.INSTANCE, aTypeSet);
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

    /**
     * Creates a new UndirectedArc as a copy of the <code>orig</code>. Only its type and attributes are copied, the
     * structural context (source, target) - is not. The specified source <code>src</code> and target <code>tar</code>
     * objects must be a part of this graph, but this is not checked here.
     */
    public Arc copyArc(final Arc orig, final Node src, final Node tar) throws TypeException {
        UndirectedArc arc = null;
        try {
            arc = (UndirectedArc) this.createArc(orig.getType(), src, tar);
            if (arc != null) {
                arc.setObjectName(orig.getObjectName());

                if (orig.getAttribute() != null) {
                    arc.createAttributeInstance();
                    ((ValueTuple) arc.getAttribute()).copyEntries(orig
                            .getAttribute());
                }
            } else {
                throw new TypeException("Graph.copyArc:: Cannot create an UndirectedArc of type : "
                        + orig.getType().getName());
            }
        } catch (TypeException ex) {
            if (src != null && tar != null) {
                throw new TypeException("   "
                        + orig.getType().getName()
                        + " from  " + src.getType().getName()
                        + " to  " + tar.getType().getName()
                        + "   " + ex.getLocalizedMessage());
            }
            throw new TypeException(ex.getLocalizedMessage());

        }
        return arc;
    }

    /**
     * Create a new Arc with given Type, source and target objects. Source and target object must be part of this graph.
     */
    protected Arc newArc(final Type t, final Node src, final Node tar) throws TypeException {
        final UndirectedArc anArc = new UndirectedArc(t, src, tar, this);
        TypeError typeError = this.itsTypes.addTypeGraphObject(anArc);
        if (typeError != null) {
            anArc.dispose();
            throw new TypeException(typeError);
        }

        this.attributed = this.attributed || anArc.getAttribute() != null;
        this.itsArcs.add(anArc);
        addArcToTypeObjectsMap(anArc);
        this.changed = true;
        if (this.notificationRequired) {
            propagateChange(new Change(Change.OBJECT_CREATED, anArc));
        }
        return anArc;
    }

    protected Arc newArcFast(Type t, Node src, Node tar) {
        try {
            return this.newArc(t, src, tar);
        } catch (TypeException ex) {
            return null;
        }
    }

    /**
     * Returns the type graph edge of the specified type <code>t</code>, with a source node of the specified type
     * <code>source</code> and a target node of the specified type <code>target</code>, otherwise returns
     * <code>null</code>.
     */
    public Arc getTypeGraphArc(final Type t, final Type source, final Type target) {
        Iterator<Arc> arcs = this.itsArcs.iterator();
        while (arcs.hasNext()) {
            Arc a = arcs.next();
            if (a.getType().compareTo(t)) {
                if (a.getSource().getType().isParentOf(source)
                        && a.getTarget().getType().isParentOf(target)) {
                    return a;
                } else if (a.getSource().getType().isParentOf(target)
                        && a.getTarget().getType().isParentOf(source)) {
                    return a;
                }
            }
        }
        return null;
    }

}
