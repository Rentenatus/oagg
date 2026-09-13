/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Arc;

/**
 * Adapter for Arc objects that implements the new XMLSerializable interface.
 * This adapter wraps an Arc instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class ArcAdapter extends DomainObjectAdapter<Arc> {
    
    /**
     * Creates a new adapter for the specified Arc.
     * 
     * @param arc The Arc to adapt
     */
    public ArcAdapter(Arc arc) {
        super(arc);
    }
    
    /**
     * Gets the adapted Arc instance.
     * 
     * @return The Arc
     */
    public Arc getArc() {
        return getDomainObject();
    }
}
