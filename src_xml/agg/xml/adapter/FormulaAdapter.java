/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.cons.Formula;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adapter for Formula objects with real DOM serialization logic.
 *
 * <p>Serializes a Formula as a {@code <Formula>} DOM element with
 * name, comment, enabled attributes and Layer/Priority children.</p>
 */
public class FormulaAdapter extends DomainObjectAdapter<Formula> {

    public FormulaAdapter(Formula formula) {
        super(formula);
    }

    public Formula getFormula() {
        return getDomainObject();
    }

    /**
     * Serializes this formula to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Formula DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Formula formula = getFormula();
        if (formula == null) {
            return null;
        }

        Element formulaElem = doc.createElement("Formula");
        String formulaId = registry.register(formula);
        formulaElem.setAttribute("ID", formulaId);
        formulaElem.setAttribute("name", formula.getName() != null ? formula.getName() : "");
        formulaElem.setAttribute("enabled", String.valueOf(formula.isEnabled()));

        // Layer
        Element layerElem = doc.createElement("Layer");
        if (formula.getLayer() != null && formula.getLayer().size() > 0) {
            layerElem.setAttribute("Layer", formula.getLayerAsString());
        } else {
            layerElem.setAttribute("Layer", "");
        }
        layerElem.setAttribute("Size",
            formula.getLayer() != null ? String.valueOf(formula.getLayer().size()) : "0");
        formulaElem.appendChild(layerElem);

        // Priority
        Element priorityElem = doc.createElement("Priority");
        if (formula.getPriority() != null && formula.getPriority().size() > 0) {
            priorityElem.setAttribute("Priority", formula.getPriorityAsString());
        } else {
            priorityElem.setAttribute("Priority", "");
        }
        priorityElem.setAttribute("Size",
            formula.getPriority() != null ? String.valueOf(formula.getPriority().size()) : "0");
        formulaElem.appendChild(priorityElem);

        return formulaElem;
    }
}
