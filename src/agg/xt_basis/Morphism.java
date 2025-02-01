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

import agg.util.Disposable;
import java.util.Iterator;

/**
 * Minimal interface for (read only) operation on a graph morphism. This interface does NOT provide method declarations
 * for the construction of a morphism, i.e. adding of object mappings, nor does it provide the capabilities of an
 * observable.
 */
public interface Morphism extends Disposable {

    /**
     * Set my name.
     */
    public abstract void setName(String n);

    /**
     * Return my name.
     */
    public abstract String getName();

    /**
     * Return my source graph.
     */
    public abstract Graph getOriginal();

    /**
     * Return my target graph.
     */
    public abstract Graph getImage();

    /**
     * Return an Iterator of the graphobjects out of my source graph which are actually taking part in one of my
     * mappings. Iterator elements are of type <code>GraphObject</code>.
     *
     * @see agg.xt_basis.GraphObject
     */
    public abstract Iterator<GraphObject> getDomain();

    /**
     * Return an Iterator of the graphobjects out of my target graph which are actually taking part in one of my
     * mappings. Iterator elements are of type <code>GraphObject</code>.
     *
     * @see agg.xt_basis.GraphObject
     */
    public abstract Iterator<GraphObject> getCodomain();

    /**
     * Return the image of the specified object.
     *
     * @return <code>null</code> if the object is not in domain.
     */
    public abstract GraphObject getImage(GraphObject o);

    /**
     * Return an Iterator of the inverse images of the specified object. The Iterator will be empty when the object is
     * not in the codomain. The elements of the Iterator are of type {@link agg.xt_basis.GraphObject}.
     *
     * @param o The object whose inverse images are to be returned.
     * @return An Iterator of the inverse images of the specified object.
     * @see agg.xt_basis.GraphObject
     */
    public abstract Iterator<GraphObject> getInverseImage(GraphObject o);

    /**
     * Return an element of the inverse images of the specified object. The Method will be send
     * IndexOutOfBoundsException when the object is not in the codomain. The elements of the Iterator are of type
     * {@link agg.xt_basis.GraphObject}.
     * 
     * Must always be used instead of getInverseImage(o).next().
     *
     * @param o The object whose inverse images are to be returned.
     * @return An element of the inverse images of the specified object.
     * @see agg.xt_basis.GraphObject
     */
    public abstract GraphObject firstOfInverseImage(GraphObject o);

    /**
     * Checks if the inverse image has any elements.
     *
     * Must always be used instead of getInverseImage(o).hasNext().
     *
     * @param o The object whose inverse images are to be returned.
     * @return true if the inverse image contains elements, false otherwise.
     */
    public abstract boolean hasInverseImage(GraphObject o);

    /**
     * Return <code>true</code> iff I am a total morphism.
     */
    public abstract boolean isTotal();

}// ##########################################################################

