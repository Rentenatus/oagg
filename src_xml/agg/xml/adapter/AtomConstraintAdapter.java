/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.cons.AtomConstraint;
import agg.xt_basis.Graph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

                // TODO: serialize AttrCondition

                atomicElem.appendChild(conclusionElem);
            }
        }

        return atomicElem;
    }
}
