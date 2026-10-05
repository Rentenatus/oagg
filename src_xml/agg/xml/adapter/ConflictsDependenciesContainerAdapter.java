/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.parser.ConflictsDependenciesContainer;
import agg.parser.CriticalPair;
import agg.parser.CriticalPairOption;
import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.parser.ParserFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Rule;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * Adapter for ConflictsDependenciesContainer objects with native DOM
 * serialization logic (.cpx format).
 *
 * <p>Serializes the container as a {@code <CriticalPairs>} element in the
 * legacy .cpx format: the complete grammar is embedded as a
 * {@code <GraphTransformationSystem>} element (reused from
 * {@link GraGraAdapter}), followed by the {@code <cpaOptions>} element and
 * the {@code <conflictContainer>}/{@code <conflictFreeContainer>} and
 * {@code <dependencyContainer>}/{@code <dependencyFreeContainer>} sections
 * with their rule sets.</p>
 *
 * <p>Deserialization mirrors {@code ConflictsDependenciesContainer.XreadObject}
 * for the same structural surface: the grammar is loaded through
 * {@link GraGraAdapter}, the containers are created through
 * {@link ParserFactory} and installed via
 * {@link ConflictsDependenciesContainer#setLoadedContainers}, and the CPA
 * options are restored via {@code addLoadedCPAOption}.</p>
 *
 * <p><b>Scope limit:</b> computed critical pair entries (rule pairs with
 * {@code <Overlapping_Pair>} content, including the overlap morphisms and
 * the layered/priority container variants) are NOT yet supported by this
 * adapter; both directions throw an explicit
 * {@link XMLSerializationException} instead of silently dropping data. The
 * legacy path still handles computed pairs.</p>
 */
public class ConflictsDependenciesContainerAdapter
        extends DomainObjectAdapter<ConflictsDependenciesContainer> {

    /**
     * Creates a new adapter for the specified container.
     *
     * @param container The ConflictsDependenciesContainer to adapt
     */
    public ConflictsDependenciesContainerAdapter(ConflictsDependenciesContainer container) {
        super(container);
    }

    /**
     * Gets the adapted container.
     *
     * @return The ConflictsDependenciesContainer
     */
    public ConflictsDependenciesContainer getContainer() {
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
        ConflictsDependenciesContainer container = getContainer();
        if (container == null || container.getGrammar() == null) {
            return null;
        }

        ExcludePairContainer epc = container.getExcludePairContainer();
        DependencyPairContainer dpc = container.getDependencyPairContainer();
        checkComputedPairsSupported(epc, dpc);

        Element cpElem = doc.createElement("CriticalPairs");
        cpElem.setAttribute("ID", registry.register(container));

        // Embedded grammar (mirrors writeGrammar)
        GraGraAdapter graAdapter = new GraGraAdapter(container.getGrammar());
        cpElem.appendChild(graAdapter.serializeGraphTransformationSystem(doc, registry));

        // cpaOptions (mirrors writeCPAoptions values from the loaded state)
        Element optionsElem = doc.createElement("cpaOptions");
        List<agg.util.Pair<String, String>> options = container.getLoadedCPAOptions();
        if (options != null) {
            for (agg.util.Pair<String, String> option : options) {
                if (option.first != null && option.second != null) {
                    optionsElem.setAttribute(option.first, option.second);
                }
            }
        }
        cpElem.appendChild(optionsElem);

        // conflictContainer / conflictFreeContainer
        if (epc != null) {
            Element conflictElem = doc.createElement("conflictContainer");
            conflictElem.setAttribute("kind", "exclude");
            appendRuleSet(conflictElem, "RuleSet", epc.getRules(), registry);
            appendRuleSet(conflictElem, "RuleSet2", epc.getRules2(), registry);
            cpElem.appendChild(conflictElem);

            if (epc.getConflictFreeContainer() != null) {
                cpElem.appendChild(doc.createElement("conflictFreeContainer"));
            }
        }

        // dependencyContainer / dependencyFreeContainer
        if (dpc != null) {
            Element dependencyElem = doc.createElement("dependencyContainer");
            dependencyElem.setAttribute("kind",
                dpc.isSwitchDependencyEnabled()
                    ? "trigger_switch_dependency" : "trigger_dependency");
            appendRuleSet(dependencyElem, "RuleSet", dpc.getRules(), registry);
            appendRuleSet(dependencyElem, "RuleSet2", dpc.getRules2(), registry);
            cpElem.appendChild(dependencyElem);

            if (dpc.getConflictFreeContainer() != null) {
                cpElem.appendChild(doc.createElement("dependencyFreeContainer"));
            }
        }

        return cpElem;
    }

    /**
     * Throws when the container holds computed critical pair entries, which
     * are not yet supported by the DOM path.
     */
    private void checkComputedPairsSupported(ExcludePairContainer epc,
            DependencyPairContainer dpc) throws XMLSerializationException {
        if (epc != null) {
            if (!epc.getExcludeContainer().isEmpty()) {
                throw new XMLSerializationException(
                    "Computed conflict entries (Overlapping_Pair content) are"
                        + " not yet supported by the DOM path for .cpx");
            }
            if (epc.getConflictFreeContainer() != null
                    && !epc.getConflictFreeContainer().isEmpty()) {
                throw new XMLSerializationException(
                    "Computed conflict-free entries are not yet supported"
                        + " by the DOM path for .cpx");
            }
        }
        if (dpc != null) {
            if (!dpc.getExcludeContainer().isEmpty()) {
                throw new XMLSerializationException(
                    "Computed dependency entries (Overlapping_Pair content)"
                        + " are not yet supported by the DOM path for .cpx");
            }
            if (dpc.getConflictFreeContainer() != null
                    && !dpc.getConflictFreeContainer().isEmpty()) {
                throw new XMLSerializationException(
                    "Computed dependency-free entries are not yet supported"
                        + " by the DOM path for .cpx");
            }
        }
    }

    /**
     * Appends a rule set element (size + rule ID references), mirroring
     * writeRuleSet.
     */
    private void appendRuleSet(Element parent, String tagname, List<Rule> ruleSet,
            DOMSerializationRegistry registry) {
        Element setElem = parent.getOwnerDocument().createElement(tagname);
        setElem.setAttribute("size", String.valueOf(ruleSet.size()));
        for (int i = 0; i < ruleSet.size(); i++) {
            String ruleId = registry.getId(ruleSet.get(i));
            if (ruleId.isEmpty()) {
                ruleId = registry.register(ruleSet.get(i));
            }
            setElem.setAttribute("i" + i, ruleId);
        }
        parent.appendChild(setElem);
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
     * Deserializes a ConflictsDependenciesContainer from a
     * {@code <CriticalPairs>} DOM element.
     *
     * @param cpElem  The CriticalPairs DOM element
     * @param registry The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element cpElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ConflictsDependenciesContainer container = getContainer();
        if (container == null || cpElem == null) {
            return;
        }

        String id = cpElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(container, id);
        }

        // Embedded grammar (mirrors readGrammar)
        GraGra grammar = agg.xt_basis.BaseFactory.theFactory().createGraGra();
        Element gtsElem = findChildElement(cpElem, "GraphTransformationSystem");
        if (gtsElem != null) {
            GraGraAdapter graAdapter = new GraGraAdapter(grammar);
            graAdapter.deserializeFromElement(gtsElem, registry);
        }
        grammar.prepareRuleInfo();
        boolean layered = grammar.isLayered();
        boolean priority = grammar.trafoByPriority();

        // cpaOptions
        Element optionsElem = findChildElement(cpElem, "cpaOptions");
        if (optionsElem != null) {
            NamedNodeMap attrs = optionsElem.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
                container.addLoadedCPAOption(attr.getName(), attr.getValue());
            }
        }

        ExcludePairContainer epc = null;
        DependencyPairContainer dpc = null;

        // conflictContainer
        Element conflictElem = findChildElement(cpElem, "conflictContainer");
        if (conflictElem == null) {
            conflictElem = findChildElement(cpElem, "conflictsContainer");
            if (conflictElem == null) {
                conflictElem = findChildElement(cpElem, "excludeContainer");
            }
        }
        if (conflictElem != null) {
            epc = (ExcludePairContainer) ParserFactory.createEmptyCriticalPairs(
                grammar, CriticalPairOption.EXCLUDEONLY, layered);
            deserializeRuleSet(conflictElem, "RuleSet", epc.getRules(), registry);
            deserializeRuleSet(conflictElem, "RuleSet2", epc.getRules2(), registry);
            checkNoPairEntries(conflictElem);
        }

        // free containers are supported empty only
        checkFreeContainerEmpty(cpElem, "conflictFreeContainer");
        checkFreeContainerEmpty(cpElem, "dependencyFreeContainer");

        // dependencyContainer
        Element dependencyElem = findChildElement(cpElem, "dependencyContainer");
        if (dependencyElem == null) {
            dependencyElem = findChildElement(cpElem, "dependenciesContainer");
        }
        if (dependencyElem != null) {
            boolean switchDependency =
                "trigger_switch_dependency".equals(dependencyElem.getAttribute("kind"));
            dpc = (DependencyPairContainer) ParserFactory.createEmptyCriticalPairs(
                grammar,
                switchDependency
                    ? CriticalPair.TRIGGER_SWITCH_DEPENDENCY
                    : CriticalPair.TRIGGER_DEPENDENCY,
                layered);
            dpc.enableSwitchDependency(switchDependency);
            deserializeRuleSet(dependencyElem, "RuleSet", dpc.getRules(), registry);
            deserializeRuleSet(dependencyElem, "RuleSet2", dpc.getRules2(), registry);
            checkNoPairEntries(dependencyElem);
        }

        container.setLoadedContainers(epc, dpc, layered, priority);
    }

    /**
     * Reads a rule set element (size + rule ID references), mirroring
     * readRuleSet.
     */
    private void deserializeRuleSet(Element parent, String tagname, List<Rule> ruleSet,
            DOMSerializationRegistry registry) {
        Element setElem = findChildElement(parent, tagname);
        if (setElem == null) {
            return;
        }
        String sizeStr = setElem.getAttribute("size");
        try {
            int size = Integer.parseInt(sizeStr);
            for (int i = 0; i < size; i++) {
                Object rule = registry.getObject(setElem.getAttribute("i" + i));
                if (rule instanceof Rule) {
                    ruleSet.add((Rule) rule);
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * Throws when the container element holds computed pair entries, which
     * are not yet supported by the DOM path.
     */
    private void checkNoPairEntries(Element containerElem) throws XMLSerializationException {
        NodeList children = containerElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "Rule".equals(child.getNodeName())) {
                throw new XMLSerializationException(
                    "Computed critical pair entries (Overlapping_Pair content)"
                        + " are not yet supported by the DOM path for .cpx");
            }
        }
    }

    /**
     * Throws when a free container element holds computed pair entries,
     * which are not yet supported by the DOM path.
     */
    private void checkFreeContainerEmpty(Element cpElem, String tagname)
            throws XMLSerializationException {
        Element freeElem = findChildElement(cpElem, tagname);
        if (freeElem != null && findChildElement(freeElem, "Rule") != null) {
            throw new XMLSerializationException(
                "Computed " + tagname + " entries are not yet supported"
                    + " by the DOM path for .cpx");
        }
    }

    private static Element findChildElement(org.w3c.dom.Node parent, String name) {
        if (!(parent instanceof Element)) {
            return null;
        }
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
