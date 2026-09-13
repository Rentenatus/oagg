/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.GraGra;

/**
 * Adapter for GraGra objects that implements the new XMLSerializable interface.
 * This adapter wraps a GraGra instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class GraGraAdapter extends DomainObjectAdapter<GraGra> implements agg.xml.core.XMLSerializable {

    public GraGraAdapter(GraGra graGra) {
        super(graGra);
    }

    public GraGra getGraGra() {
        return getDomainObject();
    }
}
