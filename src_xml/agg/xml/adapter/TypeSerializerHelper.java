/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.attribute.AttrType;
import agg.attribute.impl.DeclMember;
import agg.attribute.impl.DeclTuple;
import agg.xt_basis.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class providing shared DOM serialization logic for all Type
 * implementations (TypeImpl, NodeTypeImpl, ArcTypeImpl).
 *
 * <p>All three Type implementations have the same API (getStringRepr,
 * getAdditionalRepr, isAbstract, getAttrType) but do not share a common
 * superclass. This helper centralizes the serialization code.</p>
 */
final class TypeSerializerHelper {

    private TypeSerializerHelper() {
    }

    /**
     * Serializes a Type to a DOM element with the specified tag name.
     *
     * @param type     The Type to serialize
     * @param tagName  The DOM element tag name (NodeType, EdgeType, or Type)
     * @param doc      The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The type DOM element
     */
    static Element serializeType(Type type, String tagName,
            Document doc, DOMSerializationRegistry registry) {
        if (type == null) {
            return null;
        }

        // Build the name string
        String name = type.getStringRepr();
        String additional = type.getAdditionalRepr();
        if (additional != null && !additional.isEmpty()) {
            name = name + "%" + additional;
        }

        Element typeElem = doc.createElement(tagName);
        String typeId = registry.register(type);
        typeElem.setAttribute("ID", typeId);
        typeElem.setAttribute("name", name);
        typeElem.setAttribute("abstract", String.valueOf(type.isAbstract()));

        // Comment (getTextualComment is part of the Type interface; the
        // previous instanceof TypeImpl cast never matched the concrete
        // NodeTypeImpl / ArcTypeImpl classes, losing the comment)
        String comment = type.getTextualComment();
        if (comment != null && !comment.isEmpty()) {
            typeElem.setAttribute("comment", comment);
        }

        // Serialize Parent elements (multiple inheritance for node types)
        if (type instanceof agg.xt_basis.TypeImpl) {
            java.util.List<agg.xt_basis.Type> parents = ((agg.xt_basis.TypeImpl) type).getParents();
            if (parents != null) {
                for (agg.xt_basis.Type parent : parents) {
                    Element parentElem = doc.createElement("Parent");
                    String parentId = registry.getId(parent);
                    if (parentId.isEmpty()) {
                        parentId = registry.register(parent);
                    }
                    parentElem.setAttribute("pID", parentId);
                    typeElem.appendChild(parentElem);
                }
            }
        }

        // Serialize attribute type declarations
        AttrType attrType = type.getAttrType();
        if (attrType instanceof DeclTuple) {
            DeclTuple declTuple = (DeclTuple) attrType;
            for (int i = 0; i < declTuple.getSize(); i++) {
                DeclMember dm = (DeclMember) declTuple.getMemberAt(i);
                if (dm != null) {
                    Element attrElem = doc.createElement("AttrType");
                    String attrId = registry.register(dm);
                    attrElem.setAttribute("ID", attrId);
                    attrElem.setAttribute("attrname",
                        dm.getName() != null ? dm.getName() : "");
                    attrElem.setAttribute("typename",
                        dm.getTypeName() != null ? dm.getTypeName() : "");
                    attrElem.setAttribute("visible",
                        dm.isVisible() ? "true" : "false");
                    typeElem.appendChild(attrElem);
                }
            }
        }

        return typeElem;
    }
}
