/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Match;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Adapter for Match objects with real DOM serialization logic.
 *
 * <p>Serializes a Match as a {@code <Match>} DOM element containing
 * a Morphism with Mapping children.</p>
 */
public class MatchAdapter extends DomainObjectAdapter<Match> {

    public MatchAdapter(Match match) {
        super(match);
    }

    public Match getMatch() {
        return getDomainObject();
    }

    /**
     * Serializes this match to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Match DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Match match = getMatch();
        if (match == null) {
            return null;
        }

        Element matchElem = doc.createElement("Match");
        String matchId = registry.register(match);
        matchElem.setAttribute("ID", matchId);

        // Morphism with mappings
        RuleAdapter.serializeMorphism(match, doc, registry, matchElem);

        return matchElem;
    }

    /**
     * Deserializes a Match from a DOM element into the wrapped Match instance.
     *
     * @param matchElem The Match DOM element
     * @param registry  The ID registry for cross-references
     */
    public void deserializeFromElement(Element matchElem, DOMSerializationRegistry registry) {
        Match match = getMatch();
        if (match == null || matchElem == null) {
            return;
        }

        String id = matchElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(match, id);
        }

        // Find and deserialize the Morphism child
        NodeList children = matchElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "Morphism".equals(child.getNodeName())) {
                RuleAdapter.deserializeMorphism(match, (Element) child, registry);
                break;
            }
        }
    }
}
