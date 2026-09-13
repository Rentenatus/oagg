/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Arc;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Node;
import agg.xt_basis.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adapter for Arc objects with real DOM serialization logic.
 *
 * <p>Serializes an Arc as an {@code <Edge>} DOM element with type, source,
 * and target references as ID attributes. IDs are managed by a
 * {@link DOMSerializationRegistry}.</p>
 */
public class ArcAdapter extends DomainObjectAdapter<Arc> {

    /**
     * Creates a new adapter for the specified Arc.
     *
     * @param arc The Arc to adapt
     */
    public ArcAdapter(Arc arc) {
        super(arc);
    }

    /**
     * Gets the adapted Arc instance.
     *
     * @return The Arc
     */
    public Arc getArc() {
        return getDomainObject();
    }

    /**
     * Serializes this arc to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Edge DOM element, or null if arc is null
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Arc arc = getArc();
        if (arc == null) {
            return null;
        }

        Element edgeElem = doc.createElement("Edge");
        String arcId = registry.register(arc);
        edgeElem.setAttribute("ID", arcId);

        if (!arc.isVisible()) {
            edgeElem.setAttribute("visible", "false");
        }

        String objName = arc.getObjectName();
        if (objName != null && !objName.isEmpty()) {
            edgeElem.setAttribute("name", objName);
        }

        // Type reference
        Type type = arc.getType();
        if (type != null) {
            String typeId = registry.getId(type);
            if (typeId.isEmpty()) {
                typeId = registry.register(type);
            }
            edgeElem.setAttribute("type", typeId);
        }

        // Source reference
        GraphObject source = arc.getSource();
        if (source != null) {
            String sourceId = registry.getId(source);
            if (sourceId.isEmpty()) {
                sourceId = registry.register(source);
            }
            edgeElem.setAttribute("source", sourceId);
        }

        // Target reference
        GraphObject target = arc.getTarget();
        if (target != null) {
            String targetId = registry.getId(target);
            if (targetId.isEmpty()) {
                targetId = registry.register(target);
            }
            edgeElem.setAttribute("target", targetId);
        }

        // TODO: serialize attributes (AttrInstance) and multiplicity

        return edgeElem;
    }
}
