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
import agg.xt_basis.Type;
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
                        String text = v != null ? v.toString() : "";
                        if (text.indexOf('"') == -1) {
                            // Plain string form (legacy parity)
                            Element stringElem = doc.createElement("string");
                            stringElem.setTextContent(text);
                            valueElem.appendChild(stringElem);
                        } else {
                            // String containing quotes: use the XMLEncoder
                            // form exactly like the legacy
                            // XMLHelper.addAttrUsingXMLEncoder
                            // (<java class="java.beans.XMLDecoder">...)
                            appendXMLEncoderValue(doc, valueElem, text);
                        }
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

    /**
     * Deserializes Attribute child elements into a ValueTuple (AttrInstance).
     *
     * <p>Reads each {@code <Attribute>} child, resolves the DeclMember
     * reference, finds the corresponding ValueMember, and sets its value
     * from the {@code <Value>} child element.</p>
     *
     * @param parentElem The parent element containing Attribute children
     * @param valueTuple The attribute instance to populate
     * @param registry   The ID registry for DeclMember references
     */
    static void deserializeAttributes(Element parentElem, ValueTuple valueTuple,
            DOMSerializationRegistry registry) {
        if (parentElem == null || valueTuple == null) {
            return;
        }

        NodeList children = parentElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element attrElem = (Element) child;
            if (!"Attribute".equals(attrElem.getTagName())) {
                continue;
            }

            // Resolve DeclMember from type reference
            String typeId = attrElem.getAttribute("type");
            Object declObj = registry.getObject(typeId);
            if (!(declObj instanceof DeclMember)) {
                continue;
            }
            DeclMember decl = (DeclMember) declObj;
            String memberName = decl.getName();
            if (memberName == null) {
                continue;
            }

            // Find the ValueMember by name
            ValueMember member = valueTuple.getValueMemberAt(memberName);
            if (member == null) {
                continue;
            }

            String constant = attrElem.getAttribute("constant");
            String variable = attrElem.getAttribute("variable");

            // Find Value child element
            Element valueElem = null;
            NodeList attrChildren = attrElem.getChildNodes();
            for (int j = 0; j < attrChildren.getLength(); j++) {
                org.w3c.dom.Node vc = attrChildren.item(j);
                if (vc.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                        && "Value".equals(vc.getNodeName())) {
                    valueElem = (Element) vc;
                    break;
                }
            }
            if (valueElem == null) {
                continue;
            }

            // Read the value: find the first element child of <Value>
            // (e.g. <string>, <int>, <boolean>, etc.)
            String typeName = decl.getType() != null ? decl.getType().toString() : "String";
            NodeList valueChildren = valueElem.getChildNodes();
            for (int j = 0; j < valueChildren.getLength(); j++) {
                org.w3c.dom.Node vc = valueChildren.item(j);
                if (vc.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                Element typedElem = (Element) vc;
                String textContent = typedElem.getTextContent();
                String tagName = typedElem.getTagName();

                // Determine the value based on type
                Object value = null;
                if ("java".equals(tagName)) {
                    // XMLEncoder form (legacy XMLDecoder path): decode the
                    // embedded fragment; fall back to the inner string text
                    value = decodeXMLEncoderFragment(typedElem);
                    if (value == null) {
                        Element innerString = findChildElement(typedElem, "string");
                        value = innerString != null ? innerString.getTextContent() : null;
                    }
                } else if ("String".equals(typeName) || "string".equals(tagName) || "String".equals(tagName)) {
                    value = textContent != null ? textContent.trim() : "";
                } else if ("int".equals(typeName)) {
                    try {
                        value = Integer.parseInt(textContent.trim());
                    } catch (NumberFormatException e) {
                        value = 0;
                    }
                } else if ("boolean".equals(typeName)) {
                    value = Boolean.valueOf(textContent.trim());
                } else if ("double".equals(typeName)) {
                    try {
                        value = Double.parseDouble(textContent.trim());
                    } catch (NumberFormatException e) {
                        value = 0.0;
                    }
                } else if ("float".equals(typeName)) {
                    try {
                        value = Float.parseFloat(textContent.trim());
                    } catch (NumberFormatException e) {
                        value = 0.0f;
                    }
                } else if ("long".equals(typeName)) {
                    try {
                        value = Long.parseLong(textContent.trim());
                    } catch (NumberFormatException e) {
                        value = 0L;
                    }
                } else {
                    // Fallback: use string value
                    value = textContent != null ? textContent.trim() : "";
                }

                // Set the value on the ValueMember
                if ("true".equals(constant)) {
                    if ("String".equals(typeName)) {
                        member.setExprAsObject(value);
                    } else if (value instanceof Number || value instanceof Boolean) {
                        member.setExprAsEvaluatedText(value.toString());
                    } else {
                        member.setExprAsObject(value);
                    }
                } else if ("true".equals(variable)) {
                    member.setExprAsText(value.toString());
                } else {
                    // Expression
                    if ("String".equals(typeName)) {
                        member.setExprAsObject(value);
                    } else {
                        member.setExprAsText(value.toString());
                    }
                }
                break; // Only process first typed element child
            }
        }
    }

    /**
     * Appends the XMLEncoder form of a value to the Value element, mirroring
     * the legacy XMLHelper.addAttrUsingXMLEncoder: the value is written with
     * a java.beans.XMLEncoder and the resulting {@code <java
     * class="java.beans.XMLDecoder">} fragment is imported into the document.
     */
    private static void appendXMLEncoderValue(Document doc, Element valueElem, Object value) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.beans.XMLEncoder encoder = new java.beans.XMLEncoder(
                new java.io.BufferedOutputStream(baos));
            encoder.writeObject(value);
            encoder.close();
            javax.xml.parsers.DocumentBuilderFactory factory =
                agg.xml.util.XMLUtils.createSecureDocumentBuilderFactory(false);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            Document tmpDoc = builder.parse(
                new java.io.ByteArrayInputStream(baos.toByteArray()));
            org.w3c.dom.Node imported = doc.importNode(tmpDoc.getDocumentElement(), true);
            valueElem.appendChild(imported);
        } catch (Exception e) {
            // Fallback: plain string form
            Element stringElem = doc.createElement("string");
            stringElem.setTextContent(value != null ? value.toString() : "");
            valueElem.appendChild(stringElem);
        }
    }

    /**
     * Decodes a {@code <java class="java.beans.XMLDecoder">} fragment using
     * a java.beans.XMLDecoder, mirroring the legacy getAttrUsingXMLDecoder.
     * Returns null if decoding fails.
     */
    private static Object decodeXMLEncoderFragment(Element javaElem) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.xml.transform.Transformer transformer =
                javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            javax.xml.transform.dom.DOMSource source =
                new javax.xml.transform.dom.DOMSource(javaElem);
            javax.xml.transform.stream.StreamResult result =
                new javax.xml.transform.stream.StreamResult(baos);
            transformer.transform(source, result);
            java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(baos.toByteArray()));
            Object value = decoder.readObject();
            decoder.close();
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the first child element with the given tag name.
     */
    private static Element findChildElement(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
}
