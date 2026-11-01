/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.parser.PairContainer;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adapter for the standalone pair container format (.cpx): a single
 * {@link ExcludePairContainer} (or {@link DependencyPairContainer},
 * including the layered/priority variants) as the top object, mirroring
 * {@code ExcludePairContainer.XwriteObject}/{@code XreadObject}.
 *
 * <p>Format: {@code CriticalPairs > [GTS] > conflictsContainer} (kind
 * "exclude") or {@code dependenciesContainer} (kind
 * "trigger_dependency"/"trigger_switch_dependency") with the rule pair
 * entries and Overlapping_Pair content, followed by the
 * {@code conflictFreeContainer} section. Unlike the
 * ConflictsDependenciesContainer format there are no rule sets, no
 * cpaOptions and no ConflictDependencyGraph section, and the free
 * entries carry no status.</p>
 *
 * <p>The entry, overlap morphism and free container logic is shared with
 * {@link ConflictsDependenciesContainerAdapter}.</p>
 */
public class PairContainerAdapter extends DomainObjectAdapter<PairContainer> {

    /**
     * Creates a new adapter for the specified pair container.
     *
     * @param container The PairContainer to adapt
     */
    public PairContainerAdapter(PairContainer container) {
        super(container);
    }

    /**
     * Gets the adapted pair container.
     *
     * @return The PairContainer
     */
    public PairContainer getContainer() {
        return getDomainObject();
    }

    /**
     * Serializes this container into the serializer context.
     *
     * @param context The serializer context to write to
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        serialize(context, new DOMSerializationRegistry());
    }

    /**
     * Serializes this container into the serializer context using a
     * caller-provided registry.
     *
     * @param context  The serializer context to write to
     * @param registry The ID registry to use
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element elem = serializeToElement(
            context.getCurrentElement().getOwnerDocument(), registry);
        if (elem != null) {
            context.getCurrentElement().appendChild(elem);
        }
    }

    /**
     * Serializes this container to a {@code <CriticalPairs>} DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The CriticalPairs element, or null if there is no grammar
     * @throws XMLSerializationException if serialization fails
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ExcludePairContainer container = (ExcludePairContainer) getContainer();
        if (container == null || container.getGrammar() == null) {
            return null;
        }
        boolean dependency = container instanceof DependencyPairContainer;

        Element cpElem = doc.createElement("CriticalPairs");
        cpElem.setAttribute("ID", registry.register(container));

        // Embedded grammar (mirrors XwriteObject)
        GraGraAdapter graAdapter = new GraGraAdapter(container.getGrammar());
        cpElem.appendChild(graAdapter.serializeGraphTransformationSystem(doc, registry));

        // container section (the standalone format uses the plural tags)
        Element containerElem = doc.createElement(
            dependency ? "dependenciesContainer" : "conflictsContainer");
        if (dependency) {
            containerElem.setAttribute("kind",
                ((DependencyPairContainer) container).isSwitchDependencyEnabled()
                    ? "trigger_switch_dependency" : "trigger_dependency");
        } else {
            containerElem.setAttribute("kind", "exclude");
        }
        ConflictsDependenciesContainerAdapter.appendExcludeEntries(
            containerElem, container, registry, false);
        cpElem.appendChild(containerElem);

        // free container section: the conflict container writer emits it
        // unconditionally, the dependency writer only when it exists
        if (!dependency || container.getConflictFreeContainer() != null) {
            Element freeElem = doc.createElement("conflictFreeContainer");
            ConflictsDependenciesContainerAdapter.appendFreeEntries(
                freeElem, container, registry, false);
            cpElem.appendChild(freeElem);
        }

        return cpElem;
    }

    /**
     * Deserializes this container from the deserializer context, using a
     * fresh ID registry.
     *
     * @param context The deserializer context to read from
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        deserialize(context, new DOMSerializationRegistry());
    }

    /**
     * Deserializes this container from the deserializer context using a
     * caller-provided registry.
     *
     * @param context  The deserializer context to read from
     * @param registry The ID registry to use
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element contextElement = context.getCurrentElement();
        Element cpElement = findChildElement(contextElement, "CriticalPairs");
        if (cpElement == null) {
            cpElement = findChildElement(
                contextElement.getOwnerDocument().getDocumentElement(),
                "CriticalPairs");
        }
        if (cpElement == null) {
            throw new XMLSerializationException(
                "CriticalPairs element not found in document");
        }
        deserializeFromElement(cpElement, registry);
    }

    /**
     * Deserializes a pair container from a {@code <CriticalPairs>} DOM
     * element.
     *
     * @param cpElem  The CriticalPairs DOM element
     * @param registry The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element cpElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ExcludePairContainer container = (ExcludePairContainer) getContainer();
        if (container == null || cpElem == null) {
            return;
        }

        String id = cpElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(container, id);
        }

        // Embedded grammar: the standalone reader replaces the grammar of
        // the container
        GraGra grammar = BaseFactory.theFactory().createGraGra();
        Element gtsElem = findChildElement(cpElem, "GraphTransformationSystem");
        if (gtsElem != null) {
            GraGraAdapter graAdapter = new GraGraAdapter(grammar);
            graAdapter.deserializeFromElement(gtsElem, registry);
        }
        container.setGrammar(grammar);

        // container section: both tag families of the legacy reader
        Element containerElem = findChildElement(cpElem, "conflictsContainer");
        if (containerElem == null) {
            containerElem = findChildElement(cpElem, "conflictContainer");
        }
        if (containerElem == null) {
            containerElem = findChildElement(cpElem, "excludeContainer");
        }
        if (containerElem == null) {
            containerElem = findChildElement(cpElem, "dependenciesContainer");
        }
        if (containerElem == null) {
            containerElem = findChildElement(cpElem, "dependencyContainer");
        }
        if (containerElem != null) {
            if (container instanceof DependencyPairContainer) {
                boolean switchDependency =
                    "trigger_switch_dependency".equals(containerElem.getAttribute("kind"));
                ((DependencyPairContainer) container).enableSwitchDependency(switchDependency);
            }
            // the standalone format has no rule sets and no progress
            // indices
            ConflictsDependenciesContainerAdapter.readExcludeEntries(
                containerElem, container, grammar, registry, false);
        }

        // free container section: the standalone reader restores only the
        // DISABLED state of disabled rules (no NOT_RELATED handling)
        Element freeElem = findChildElement(cpElem, "conflictFreeContainer");
        if (freeElem != null) {
            ConflictsDependenciesContainerAdapter.readFreeEntries(
                freeElem, container, registry, false, false, false);
        }
    }

    private static Element findChildElement(org.w3c.dom.Node parent, String name) {
        if (!(parent instanceof Element)) {
            return null;
        }
        org.w3c.dom.NodeList children = parent.getChildNodes();
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
