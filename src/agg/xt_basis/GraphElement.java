/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import agg.util.XMLHelper;

/**
 * GraphElement defines the common interface for all elements in a graph,
 * including Nodes and Arcs. This interface provides the fundamental operations
 * that are shared across all graph elements.
 *
 * @author Janusch Rentenatus
 */
public interface GraphElement {

    /**
     * Returns whether this element is an arc.
     *
     * @return true if this element is an arc, false otherwise
     */
    boolean isArc();

    /**
     * Returns whether this element is a node.
     *
     * @return true if this element is a node, false otherwise
     */
    boolean isNode();

    /**
     * Returns the graph context this element belongs to.
     *
     * @return the graph context, or null if not set
     */
    Graph getContext();

    /**
     * Returns the type of this element.
     *
     * @return the type of this element
     */
    Type getType();

    /**
     * Sets the type of this element.
     *
     * @param type the type to set
     */
    void setType(Type type);

    /**
     * Returns the attribute instance of this element.
     *
     * @return the attribute instance, or null if not set
     */
    agg.attribute.AttrInstance getAttribute();

    /**
     * Checks if this element has attributes.
     *
     * @return true if attributes exist, false otherwise
     */
    boolean attrExists();

    /**
     * Converts this element to a key string for comparison purposes.
     *
     * @return the key string representation
     */
    String convertToKey();

    /**
     * Resets and returns the type key for this element.
     *
     * @return the reset type key
     */
    String resetTypeKey();

    /**
     * Writes this element to XML.
     *
     * @param xmlHelper the XML helper to write with
     */
    void XwriteObject(XMLHelper xmlHelper);

    /**
     * Reads this element from XML.
     *
     * @param xmlHelper the XML helper to read from
     */
    void XreadObject(XMLHelper xmlHelper);

    /**
     * Compares this element to another for equality.
     *
     * @param otherObject the graph object to compare with
     * @return true if elements are equal, false otherwise
     */
    boolean compareTo(GraphObject otherObject);
}
