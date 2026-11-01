/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.TypeGraph;

/**
 * Adapter for TypeGraph objects that implements the new XMLSerializable interface.
 */
public class TypeGraphAdapter extends DomainObjectAdapter<TypeGraph> {

    public TypeGraphAdapter(TypeGraph typeGraph) {
        super(typeGraph);
    }

    public TypeGraph getTypeGraph() {
        return getDomainObject();
    }
}
