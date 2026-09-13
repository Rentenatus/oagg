/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.TypeImpl;

/**
 * Adapter for TypeImpl objects that implements the new XMLSerializable interface.
 */
public class TypeImplAdapter extends DomainObjectAdapter<TypeImpl> {

    public TypeImplAdapter(TypeImpl typeImpl) {
        super(typeImpl);
    }

    public TypeImpl getTypeImpl() {
        return getDomainObject();
    }
}
