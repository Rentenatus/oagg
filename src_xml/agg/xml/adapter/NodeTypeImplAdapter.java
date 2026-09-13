/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.NodeTypeImpl;

/**
 * Adapter for NodeTypeImpl objects that implements the new XMLSerializable interface.
 */
public class NodeTypeImplAdapter extends DomainObjectAdapter<NodeTypeImpl> {

    public NodeTypeImplAdapter(NodeTypeImpl nodeTypeImpl) {
        super(nodeTypeImpl);
    }

    public NodeTypeImpl getNodeTypeImpl() {
        return getDomainObject();
    }
}
