/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * ****************************************************************************
 */
package agg.convert;

import agg.attribute.handler.AttrHandler;
import agg.attribute.handler.impl.javaExpr.JexHandler;
import agg.attribute.impl.AttrTupleManager;
import agg.attribute.impl.VarMember;
import agg.attribute.impl.VarTuple;
import agg.xt_basis.GraGra;
import agg.xt_basis.Node;
import agg.xt_basis.NodeTypeImpl;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import agg.xt_basis.TypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Converts the schema part of a WSDL document into an AGG grammar
 * (node types with attributes and one rule per non-response element).
 * The navigation mirrors the former XMLHelper-based reader: the first
 * child element must be {@code wsdl:types}, its {@code s:schema}
 * children are converted in document order.
 */
public class WSDL2ggx {

    private GraGra gragra;
    private boolean done;

    public WSDL2ggx(GraGra gra) {
        this.gragra = gra;
    }

    public boolean isSuccessful() {
        return done;
    }

    private Node createNodeType(String name) {
        NodeTypeImpl nt = (NodeTypeImpl) gragra.getTypeSet().createNodeType(true);
        nt.setStringRepr(name);
        try {
            return gragra.getTypeSet().getTypeGraph().createNode(nt);
        } catch (TypeException ex) {
            return null;
        }
    }

    private void addAttr(AttrHandler attrHandler, Type nodeType, String aType, String aName) {
        nodeType.getAttrType().addMember(attrHandler, aType, aName);
    }

    private void addVar(AttrHandler attrHandler, VarTuple varTuple, String aType, String aName, boolean inputPar) {
        varTuple.getType().addMember(attrHandler, aType, aName);
        VarMember var = (VarMember) varTuple.getMemberAt(aName);
        var.setInputParameter(inputPar);
    }

    private String getJavaType(String sType) {
        if (sType.startsWith("s:")) {
            String str = sType.substring(2);
            if (str.equals("string")) {
                str = "String";
            }
            return str;
        }
        return "";
    }

    private static Element firstChildElement(Element parent) {
        org.w3c.dom.Node n = parent.getFirstChild();
        while (n != null && n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        return (Element) n;
    }

    private static Element findChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && name.equals(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    private static java.util.List<Element> childElements(Element parent) {
        java.util.List<Element> result = new java.util.ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                result.add((Element) n);
            }
        }
        return result;
    }

    public void read(Document doc) {
        Element root = doc.getDocumentElement();
        if (root == null) {
            return;
        }
        Element types = firstChildElement(root);
        if (types == null || !"wsdl:types".equals(types.getNodeName())) {
            return;
        }
        this.done = true;
        AttrHandler attrHandler = AttrTupleManager.getDefaultManager()
                .getHandler(JexHandler.getLabelName());
        createNodeType("WebService");
        Element schema = findChild(types, "s:schema");
        if (schema == null) {
            return;
        }
        for (Element el : childElements(schema)) {
            String tag = el.getNodeName();
            if ("s:element".equals(tag)) {
                String eName = el.getAttribute("name");
                System.out.println(tag + "     " + eName);
                if (!eName.endsWith("Response")) {
                    Rule r = gragra.createRule();
                    r.setName(eName);
                    VarTuple vars = (VarTuple) r.getAttrContext().getVariables();
                    Element complexType = findChild(el, "s:complexType");
                    if (complexType != null) {
                        Element sequence = findChild(complexType, "s:sequence");
                        if (sequence != null) {
                            for (Element attr : childElements(sequence)) {
                                String aName = attr.getAttribute("name");
                                String sType = attr.getAttribute("type");
                                String aType = getJavaType(sType);
                                if (!aName.equals("") && !aType.equals("")) {
                                    addVar(attrHandler, vars, aType, aName, true);
                                }
                            }
                        }
                    }
                    r.initSignatur();
                }
            } else if ("s:complexType".equals(tag)) {
                String eName = el.getAttribute("name");
                System.out.println(tag + "     " + eName);
                if (!eName.startsWith("Array")) {
                    Node n = createNodeType(eName);
                    Element sequence = findChild(el, "s:sequence");
                    if (sequence != null) {
                        for (Element attr : childElements(sequence)) {
                            String aName = attr.getAttribute("name");
                            String sType = attr.getAttribute("type");
                            String aType = getJavaType(sType);
                            if (n != null && !aName.equals("") && !aType.equals("")) {
                                addAttr(attrHandler, n.getType(), aType, aName);
                            }
                        }
                    }
                }
            }
        }
    }
}
