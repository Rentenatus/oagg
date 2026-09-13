/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Node;
import agg.xt_basis.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adapter for Node objects with real DOM serialization logic.
 *
 * <p>Serializes a Node as a {@code <Node>} DOM element with type reference
 * as an ID attribute. The ID is managed by a {@link DOMSerializationRegistry}.</p>
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

    /**
     * Serializes this node to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for type references
     * @return The Node DOM element, or null if node is null
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Node node = getNode();
        if (node == null) {
            return null;
        }

        Element nodeElem = doc.createElement("Node");
        String nodeId = registry.register(node);
        nodeElem.setAttribute("ID", nodeId);

        if (!node.isVisible()) {
            nodeElem.setAttribute("visible", "false");
        }

        String objName = node.getObjectName();
        if (objName != null && !objName.isEmpty()) {
            nodeElem.setAttribute("name", objName);
        }

        // Type reference
        Type type = node.getType();
        if (type != null) {
            String typeId = registry.getId(type);
            if (typeId.isEmpty()) {
                typeId = registry.register(type);
            }
            nodeElem.setAttribute("type", typeId);
        }

        // TODO: serialize attributes (AttrInstance)

        return nodeElem;
    }
}
