/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.attribute.handler.HandlerExpr;
import agg.attribute.impl.DeclMember;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.ValueTuple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class for serializing and deserializing AttrInstance (ValueTuple)
 * as {@code <Attribute>} DOM elements.
 *
 * <p>Attribute XML structure (version 1.0):
 * <pre>{@code
 * <Attribute constant="true" type="I3">
 *     <Value>
 *         <string>Otti</string>
 *     </Value>
 * </Attribute>
 * }</pre></p>
 *
 * <p>For variables:
 * <pre>{@code
 * <Attribute type="I4" variable="true">
 *     <Value>
 *         <string>x</string>
 *     </Value>
 * </Attribute>
 * }</pre></p>
 */
final class AttributeSerializer {

    private AttributeSerializer() {
    }

    /**
     * Serializes a ValueTuple (AttrInstance) as Attribute child elements.
     *
     * @param valueTuple The attribute instance to serialize
     * @param doc        The DOM document to create elements in
     * @param registry   The ID registry for DeclMember references
     * @param parent     The parent element to append Attribute children to
     */
    static void serializeAttributes(ValueTuple valueTuple, Document doc,
            DOMSerializationRegistry registry, Element parent) {
        if (valueTuple == null || valueTuple.isEmpty()) {
            return;
        }

        int num = valueTuple.getSize();
        for (int i = 0; i < num; i++) {
            ValueMember val = valueTuple.getValueMemberAt(i);
            if (val == null || !val.isSet()) {
                continue;
            }

            DeclMember decl = (DeclMember) val.getDeclaration();
            if (decl == null || decl.getType() == null) {
                continue;
            }

            Element attrElem = doc.createElement("Attribute");

            // Type reference (DeclMember ID)
            String typeId = registry.getId(decl);
            if (typeId.isEmpty()) {
                typeId = registry.register(decl);
            }
            attrElem.setAttribute("type", typeId);

            HandlerExpr expr = val.getExpr();
            if (expr != null) {
                if (expr.isConstant()) {
                    Object v = expr.getValue();
                    Element valueElem = doc.createElement("Value");
                    String typeName = decl.getType().toString();
                    if ("String".equals(typeName)) {
                        // Handle nested HandlerExpr wrappers
                        while (v instanceof HandlerExpr) {
                            v = ((HandlerExpr) v).getValue();
                        }
                        Element stringElem = doc.createElement("string");
                        stringElem.setTextContent(v != null ? v.toString() : "");
                        valueElem.appendChild(stringElem);
                    } else {
                        Element typedElem = doc.createElement(typeName.toLowerCase());
                        typedElem.setTextContent(v != null ? v.toString() : "");
                        valueElem.appendChild(typedElem);
                    }
                    attrElem.appendChild(valueElem);
                    attrElem.setAttribute("constant", "true");
                } else if (expr.isVariable()) {
                    Object v = expr.getString();
                    Element valueElem = doc.createElement("Value");
                    Element stringElem = doc.createElement("string");
                    stringElem.setTextContent(v != null ? v.toString() : "");
                    valueElem.appendChild(stringElem);
                    attrElem.appendChild(valueElem);
                    attrElem.setAttribute("variable", "true");
                } else {
                    // Expression
                    Object v = expr.getString();
                    Element valueElem = doc.createElement("Value");
                    Element stringElem = doc.createElement("string");
                    stringElem.setTextContent(v != null ? v.toString() : "");
                    valueElem.appendChild(stringElem);
                    attrElem.appendChild(valueElem);
                }
            }

            parent.appendChild(attrElem);
        }
    }
}
