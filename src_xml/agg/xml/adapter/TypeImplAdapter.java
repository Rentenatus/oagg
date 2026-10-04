/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.TypeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Adapter for TypeImpl objects with real DOM serialization logic.
 *
 * <p>Serializes a TypeImpl as a {@code <NodeType>}, {@code <EdgeType>}, or
 * {@code <Type>} DOM element depending on the string representation. Includes
 * attribute type declarations as child {@code <AttrType>} elements.</p>
 */
public class TypeImplAdapter extends DomainObjectAdapter<TypeImpl> {

    /**
     * Creates a new adapter for the specified TypeImpl.
     *
     * @param typeImpl The TypeImpl to adapt
     */
    public TypeImplAdapter(TypeImpl typeImpl) {
        super(typeImpl);
    }

    /**
     * Gets the adapted TypeImpl instance.
     *
     * @return The TypeImpl
     */
    public TypeImpl getTypeImpl() {
        return getDomainObject();
    }

    /**
     * Serializes this type to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The type DOM element, or null if type is null
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        TypeImpl type = getTypeImpl();
        if (type == null) {
            return null;
        }

        // Determine element tag based on type kind
        String name = type.getStringRepr();
        String additional = type.getAdditionalRepr();
        if (additional != null && !additional.isEmpty()) {
            name = name + "%" + additional;
        }

        String tagName;
        if (name.indexOf("[NODE]") >= 0) {
            tagName = "NodeType";
        } else if (name.indexOf("[EDGE]") >= 0) {
            tagName = "EdgeType";
        } else {
            tagName = "Type";
        }

        return TypeSerializerHelper.serializeType(type, tagName, doc, registry);
    }

    /**
     * Deserializes a TypeImpl from a DOM element.
     * Note: This method assumes the TypeImpl has already been created
     * (e.g. via TypeSet.createNodeType/createEdgeType) and only reads
     * attribute values and child AttrType elements.
     *
     * @param typeElem The type DOM element
     * @param registry  The ID registry for cross-references
     */
    public void deserializeFromElement(Element typeElem, DOMSerializationRegistry registry) {
        TypeImpl type = getTypeImpl();
        if (type == null || typeElem == null) {
            return;
        }

        String id = typeElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(type, id);
        }

        // Comment (mirroring the legacy Type.XreadObject behaviour)
        String comment = typeElem.getAttribute("comment");
        if (comment != null && !comment.isEmpty()) {
            type.setTextualComment(comment);
        }

        // Parse AttrType children
        NodeList children = typeElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            if ("AttrType".equals(childElem.getTagName())) {
                String attrId = childElem.getAttribute("ID");
                String attrName = childElem.getAttribute("attrname");
                String typeName = childElem.getAttribute("typename");
                // AttrType members are created during type creation,
                // we just register them for reference resolution
                // TODO: resolve and register existing DeclMember by name
            }
        }
    }
}
