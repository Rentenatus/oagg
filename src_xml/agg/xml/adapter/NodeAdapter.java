/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Node;

/**
 * Adapter for Node objects that implements the new XMLSerializable interface.
 * This adapter wraps a Node instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class NodeAdapter extends DomainObjectAdapter<Node> {
    
    /**
     * Creates a new adapter for the specified Node.
     * 
     * @param node The Node to adapt
     */
    public NodeAdapter(Node node) {
        super(node);
    }
    
    /**
     * Gets the adapted Node instance.
     * 
     * @return The Node
     */
    public Node getNode() {
        return getDomainObject();
    }
}
