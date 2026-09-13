/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.ArcTypeImpl;

/**
 * Adapter for ArcTypeImpl objects that implements the new XMLSerializable interface.
 */
public class ArcTypeImplAdapter extends DomainObjectAdapter<ArcTypeImpl> implements agg.xml.core.XMLSerializable {

    public ArcTypeImplAdapter(ArcTypeImpl arcTypeImpl) {
        super(arcTypeImpl);
    }

    public ArcTypeImpl getArcTypeImpl() {
        return getDomainObject();
    }
}
