/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.cons.AtomConstraint;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.Graph;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Enumeration;

/**
 * Adapter for AtomConstraint objects with real DOM serialization logic.
 *
 * <p>Serializes an AtomConstraint as a {@code <Graphconstraint_Atomic>} DOM
 * element with Premise and Conclusion children. Each conclusion contains
 * a Graph, Morphism, and optional AttrCondition.</p>
 */
public class AtomConstraintAdapter extends DomainObjectAdapter<AtomConstraint> {

    public AtomConstraintAdapter(AtomConstraint constraint) {
        super(constraint);
    }

    public AtomConstraint getAtomConstraint() {
        return getDomainObject();
    }

    /**
     * Serializes this atom constraint to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Graphconstraint_Atomic DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        AtomConstraint constraint = getAtomConstraint();
        if (constraint == null) {
            return null;
        }

        Element atomicElem = doc.createElement("Graphconstraint_Atomic");
        String atomicId = registry.register(constraint);
        atomicElem.setAttribute("ID", atomicId);
        atomicElem.setAttribute("name", constraint.getAtomicName() != null
            ? constraint.getAtomicName() : "");

        if (constraint.getConclusionsSize() > 0) {
            // Premise (source graph of the first conclusion)
            Element premiseElem = doc.createElement("Premise");
            Graph premiseGraph = constraint.getSource();
            if (premiseGraph != null) {
                premiseGraph.setKind("PREMISE");
                GraphAdapter premiseAdapter = new GraphAdapter(premiseGraph);
                Element premiseGraphElem = premiseAdapter.serializeToElement(doc, registry);
                if (premiseGraphElem != null) {
                    premiseElem.appendChild(premiseGraphElem);
                }
            }
            atomicElem.appendChild(premiseElem);

            // Conclusions
            Enumeration<AtomConstraint> conclusions = constraint.getConclusions();
            while (conclusions.hasMoreElements()) {
                AtomConstraint conclusion = conclusions.nextElement();
                Element conclusionElem = doc.createElement("Conclusion");

                Graph conclusionGraph = conclusion.getImage();
                if (conclusionGraph != null) {
                    conclusionGraph.setKind("CONCLUSION");
                    GraphAdapter concAdapter = new GraphAdapter(conclusionGraph);
                    Element concGraphElem = concAdapter.serializeToElement(doc, registry);
                    if (concGraphElem != null) {
                        conclusionElem.appendChild(concGraphElem);
                    }
                }

                // Conclusion morphism
                RuleAdapter.serializeMorphism(conclusion, doc, registry, conclusionElem);

                // Serialize AttrCondition if present
                agg.attribute.AttrConditionTuple condTuple = conclusion.getAttrContext().getConditions();
                if (condTuple instanceof agg.attribute.impl.CondTuple) {
                    agg.attribute.impl.CondTuple ct = (agg.attribute.impl.CondTuple) condTuple;
                    if (ct.getSize() > 0) {
                        Element attrCondElem = doc.createElement("AttrCondition");
                        for (int k = 0; k < ct.getSize(); k++) {
                            agg.attribute.impl.CondMember cm = ct.getCondMemberAt(k);
                            if (cm != null && cm.isSet()) {
                                Element condElem = doc.createElement("Condition");
                                Element valueElem = doc.createElement("Value");
                                Element stringElem = doc.createElement("string");
                                stringElem.setTextContent(cm.getExprAsText() != null ? cm.getExprAsText() : "");
                                valueElem.appendChild(stringElem);
                                condElem.appendChild(valueElem);
                                attrCondElem.appendChild(condElem);
                            }
                        }
                        conclusionElem.appendChild(attrCondElem);
                    }
                }

                atomicElem.appendChild(conclusionElem);
            }
        }

        return atomicElem;
    }

    /**
     * Deserializes an AtomConstraint from a DOM element.
     *
     * <p>Reads the name, loads the Premise graph, and creates Conclusion
     * children with their graphs and morphisms.</p>
     *
     * @param atomicElem The Graphconstraint_Atomic DOM element
     * @param registry   The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element atomicElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        AtomConstraint constraint = getAtomConstraint();
        if (constraint == null || atomicElem == null) {
            return;
        }

        String id = atomicElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(constraint, id);
        }

        String name = atomicElem.getAttribute("name");
        if (name != null) {
            constraint.setAtomicName(name);
        }

        NodeList children = atomicElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("Premise".equals(tagName)) {
                // Load premise graph
                Element premiseGraphElem = findChildElement(childElem, "Graph");
                if (premiseGraphElem != null && constraint.getSource() != null) {
                    constraint.getSource().setName("Premise of " + name);
                    GraphAdapter premiseAdapter = new GraphAdapter(constraint.getSource());
                    premiseAdapter.deserializeFromElement(premiseGraphElem, registry);
                }
            } else if ("Conclusion".equals(tagName)) {
                // Create a new conclusion
                Graph conclusionGraph = BaseFactory.theFactory().createGraph(
                    constraint.getSource().getTypeSet());
                AtomConstraint conclusion = constraint.createNextConclusion(conclusionGraph);

                // Load conclusion graph
                Element concGraphElem = findChildElement(childElem, "Graph");
                if (concGraphElem != null && conclusionGraph != null) {
                    conclusionGraph.setName("Conclusion of " + name);
                    GraphAdapter concAdapter = new GraphAdapter(conclusionGraph);
                    concAdapter.deserializeFromElement(concGraphElem, registry);
                }

                // Load conclusion morphism
                Element concMorphElem = findChildElement(childElem, "Morphism");
                if (concMorphElem != null) {
                    RuleAdapter.deserializeMorphism(conclusion, concMorphElem, registry);
                }

                // Load AttrCondition if present
                Element attrCondElem = findChildElement(childElem, "AttrCondition");
                if (attrCondElem != null) {
                    agg.attribute.AttrConditionTuple condTuple =
                        conclusion.getAttrContext().getConditions();
                    if (condTuple instanceof agg.attribute.impl.CondTuple) {
                        agg.attribute.impl.CondTuple ct =
                            (agg.attribute.impl.CondTuple) condTuple;
                        NodeList condChildren = attrCondElem.getChildNodes();
                        for (int j = 0; j < condChildren.getLength(); j++) {
                            org.w3c.dom.Node condNode = condChildren.item(j);
                            if (condNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                                continue;
                            }
                            Element condElem = (Element) condNode;
                            if ("Condition".equals(condElem.getTagName())) {
                                Element valueElem = findChildElement(condElem, "Value");
                                if (valueElem != null) {
                                    Element stringElem = findChildElement(valueElem, "string");
                                    if (stringElem != null) {
                                        String exprText = stringElem.getTextContent();
                                        if (exprText != null && !exprText.isEmpty()) {
                                            ct.addCondition(exprText.trim());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
