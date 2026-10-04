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
        registry.bindElement(arc, edgeElem);

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

        // Serialize attributes (AttrInstance)
        agg.attribute.AttrInstance attrInst = arc.getAttribute();
        if (attrInst instanceof agg.attribute.impl.ValueTuple) {
            AttributeSerializer.serializeAttributes(
                (agg.attribute.impl.ValueTuple) attrInst, doc, registry, edgeElem);
        }

        // Type graph multiplicity (sourcemin/targetmin/sourcemax/targetmax)
        agg.xt_basis.Graph context = arc.getContext();
        if (context != null && context.isTypeGraph()) {
            agg.xt_basis.Type sourceType = arc.getSource().getType();
            agg.xt_basis.Type targetType = arc.getTarget().getType();
            int sourcemin = type.getSourceMin(sourceType, targetType);
            if (sourcemin != agg.xt_basis.Type.UNDEFINED) {
                edgeElem.setAttribute("sourcemin", Integer.toString(sourcemin));
            }
            int targetmin = type.getTargetMin(sourceType, targetType);
            if (targetmin != agg.xt_basis.Type.UNDEFINED) {
                edgeElem.setAttribute("targetmin", Integer.toString(targetmin));
            }
            int sourcemax = type.getSourceMax(sourceType, targetType);
            if (sourcemax != agg.xt_basis.Type.UNDEFINED) {
                edgeElem.setAttribute("sourcemax", Integer.toString(sourcemax));
            }
            int targetmax = type.getTargetMax(sourceType, targetType);
            if (targetmax != agg.xt_basis.Type.UNDEFINED) {
                edgeElem.setAttribute("targetmax", Integer.toString(targetmax));
            }
        }

        return edgeElem;
    }
}
