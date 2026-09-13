/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.NodeTypeImpl;

/**
 * Adapter for NodeTypeImpl objects with real DOM serialization logic.
 *
 * <p>Serializes a NodeTypeImpl as a {@code <NodeType>} DOM element.
 * Delegates to {@link TypeImplAdapter} for shared serialization logic
 * via the {@link TypeSerializerHelper}.</p>
 */
public class NodeTypeImplAdapter extends DomainObjectAdapter<NodeTypeImpl> {

    public NodeTypeImplAdapter(NodeTypeImpl type) {
        super(type);
    }

    public NodeTypeImpl getNodeTypeImpl() {
        return getDomainObject();
    }

    /**
     * Serializes this node type to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The NodeType DOM element
     */
    public org.w3c.dom.Element serializeToElement(org.w3c.dom.Document doc,
            DOMSerializationRegistry registry) {
        NodeTypeImpl type = getNodeTypeImpl();
        if (type == null) {
            return null;
        }
        return TypeSerializerHelper.serializeType(type, "NodeType", doc, registry);
    }
}
