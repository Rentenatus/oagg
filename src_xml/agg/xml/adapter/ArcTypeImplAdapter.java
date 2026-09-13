/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.ArcTypeImpl;

/**
 * Adapter for ArcTypeImpl objects with real DOM serialization logic.
 *
 * <p>Serializes an ArcTypeImpl as an {@code <EdgeType>} DOM element.
 * Delegates to {@link TypeSerializerHelper} for shared serialization logic.</p>
 */
public class ArcTypeImplAdapter extends DomainObjectAdapter<ArcTypeImpl> {

    public ArcTypeImplAdapter(ArcTypeImpl type) {
        super(type);
    }

    public ArcTypeImpl getArcTypeImpl() {
        return getDomainObject();
    }

    /**
     * Serializes this edge type to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The EdgeType DOM element
     */
    public org.w3c.dom.Element serializeToElement(org.w3c.dom.Document doc,
            DOMSerializationRegistry registry) {
        ArcTypeImpl type = getArcTypeImpl();
        if (type == null) {
            return null;
        }
        return TypeSerializerHelper.serializeType(type, "EdgeType", doc, registry);
    }
}
